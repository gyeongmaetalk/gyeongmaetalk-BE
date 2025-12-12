package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
import auctionTalk.auction.domain.review.dto.response.*;
import auctionTalk.auction.domain.review.entity.*;
import auctionTalk.auction.domain.review.mapper.ReviewMapper;
import auctionTalk.auction.domain.review.repository.ReviewImageRepository;
import auctionTalk.auction.domain.review.repository.ReviewReportRepository;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.validation.ParamValidator;
import auctionTalk.auction.utils.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final CounselRepository counselRepository;
    private final CounselorRepository counselorRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public ReviewIdResponse createReview(
            ReviewCreateRequest request, Member member
    ) {

        Counsel counsel = counselRepository.getCounselByMember(member);

        Review newReview = createAndSaveReview(request, member, counsel);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = request.getImageUrls().stream()
                    .map(url -> reviewMapper.toReviewImage(newReview, url))
                    .toList();

            reviewImageRepository.saveAll(images);
        }

        return new ReviewIdResponse(newReview.getId());
    }

    @Override
    @Transactional
    public ReviewIdResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long memberId) {

        // 수정 권한 유효성 검사(본인이 아닌 경우 수정 불가)
        Review review = reviewRepository.getReview(reviewId);
        ParamValidator.validModify(review.getMember().getId(), memberId);

        review.updateReviewInfo(request);

        List<String> remainKeys = Optional.ofNullable(request.getRemainImageUrls())
                .orElseGet(ArrayList::new);

        List<String> addKeys = Optional.ofNullable(request.getAddImageUrls())
                .orElseGet(ArrayList::new);

        List<String> deleteKeys = review.updateImages(remainKeys, addKeys);

        if (!deleteKeys.isEmpty()) {
            s3Service.deleteFiles(deleteKeys);
        }

        return new  ReviewIdResponse(review.getId());
    }

    @Override
    @Transactional
    public ReviewIdResponse deleteReview(Long reviewId, Long memberId){

        // 수정 권한 유효성 검사(본인이 아닌 경우 수정 불가)
        Review review = reviewRepository.getReview(reviewId);
        ParamValidator.validModify(review.getMember().getId(), memberId);

        reviewRepository.deleteById(reviewId);

        return new ReviewIdResponse(reviewId);
    }

    @Override
    @Transactional
    public ReviewIdResponse reportReview(Long reviewId, Member member, ReportType reasonType, String reason){

        Review review = reviewRepository.getReview(reviewId);

        ReviewReport reviewReport = reviewMapper.toReviewReport(reasonType,reason, member, review);

        reviewReportRepository.save(reviewReport);

        return new ReviewIdResponse(review.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AllReviewPagingResponse<ReviewSummaryResponse> inquiryReviews(Member member, ReviewSortType sortType, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviewPage = switch (sortType) {
            case LATEST -> reviewRepository.findLatest(pageable);
            case OLDEST -> reviewRepository.findOldest(pageable);
            case HIGHEST_SCORE ->  reviewRepository.findHighestRating(pageable);
            case LOWEST_SCORE ->  reviewRepository.findLowestRating(pageable);
            default ->  throw new CustomApiException(ErrorCode.INVALID_REVIEW_SORT_TYPE);
        };

        return reviewMapper.toAllReviewPagingResponse(
                reviewPage.map(review -> reviewMapper.toReviewSummaryResponse(review, member))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewPagingResponse<ReviewSummaryResponse> inquiryReviewsByCounselor(Long counselorId, Member member, ReviewSortType sortType, int page, int size){

        Pageable pageable = PageRequest.of(page, size);
        Counselor counselor = counselorRepository.getCounselor(counselorId);

        Page<Review> reviewPage = switch (sortType) {
            case LATEST -> reviewRepository.findLatestByCounselorId(counselorId, pageable);
            case OLDEST -> reviewRepository.findOldestByCounselorId(counselorId, pageable);
            case HIGHEST_SCORE ->  reviewRepository.findHighestRatingByCounselorId(counselorId, pageable);
            case LOWEST_SCORE ->  reviewRepository.findLowestRatingByCounselorId(counselorId, pageable);
            default ->  throw new CustomApiException(ErrorCode.INVALID_REVIEW_SORT_TYPE);
        };

        return reviewMapper.toReviewPagingResponse(
                reviewPage.map(review -> reviewMapper.toReviewSummaryResponse(review, member)), counselor
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AllReviewPagingResponse<MyReviewSummaryResponse> inquiryReviewsByMember(Member member, int page, int size){
        Page<Review> reviewPage= reviewRepository.findReviewsByMember(member, PageRequest.of(page, size));

        return reviewMapper.toAllReviewPagingResponse(
                reviewPage.map(review -> reviewMapper.toMyReviewSummaryResponse(review, member))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponse inquiryReviewDetail(Long reviewId, Member member){

        Review review = reviewRepository.getReview(reviewId);

        return reviewMapper.toReviewDetailResponse(review, member);
    }

    private Review createAndSaveReview(ReviewCreateRequest request, Member member, Counsel counsel) {
        Review newReview = reviewMapper.toReview(request, member, counsel);
        return reviewRepository.save(newReview);
    }
}
