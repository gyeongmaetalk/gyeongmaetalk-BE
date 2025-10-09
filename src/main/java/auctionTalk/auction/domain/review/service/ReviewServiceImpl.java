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
import auctionTalk.auction.domain.review.repository.ReviewReportRepository;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.validation.ParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final CounselRepository counselRepository;
    private final CounselorRepository counselorRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final ReviewImageService reviewImageService;

    @Override
    @Transactional
    public ReviewIdResponse createReview(
            ReviewCreateRequest request, List<MultipartFile> reviewImages, Member member
    ) {

        Counsel counsel = counselRepository.getCounselByMember(member);

        Review newReview = createAndSaveReview(request, member, counsel);

        if (reviewImages != null) {
            List<ReviewImage> newReviewImages = reviewImageService.createAndSaveReviewImages(newReview, reviewImages);
            newReview.changeImages(newReviewImages);
        }

        return new ReviewIdResponse(newReview.getId());
    }

    @Override
    @Transactional
    public ReviewIdResponse updateReview(Long reviewId, ReviewUpdateRequest request, List<MultipartFile> newImages, Long memberId) {

        // 수정 권한 유효성 검사(본인이 아닌 경우 수정 불가)
        Review review = reviewRepository.getReview(reviewId);
        ParamValidator.validModify(review.getMember().getId(), memberId);

        review.updateReviewInfo(request);

        // 이미지 업데이트
        if (newImages != null && !newImages.isEmpty()) {
            reviewImageService.updateReviewImages(review, request.getExistingImages(), newImages);
        }

        return new  ReviewIdResponse(review.getId());
    }

    @Override
    @Transactional
    public ReviewIdResponse deleteReview(Long reviewId, Long memberId){

        // 수정 권한 유효성 검사(본인이 아닌 경우 수정 불가)
        Review review = reviewRepository.getReview(reviewId);
        ParamValidator.validModify(review.getMember().getId(), memberId);

        reviewImageService.deleteExistingImages(review.getImages());

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
    public ReviewDetailResponse inquiryReviewDetail(Long reviewId, Member member){

        Review review = reviewRepository.getReview(reviewId);

        return reviewMapper.toReviewDetailResponse(review, member);
    }

    private Review createAndSaveReview(ReviewCreateRequest request, Member member, Counsel counsel) {
        Review newReview = reviewMapper.toReview(request, member, counsel);
        return reviewRepository.save(newReview);
    }
}
