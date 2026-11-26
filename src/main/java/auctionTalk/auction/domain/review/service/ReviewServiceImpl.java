package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewCursor;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final int MAX_CURSOR_PAGE_SIZE = 50;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "reviewList", allEntries = true),
            @CacheEvict(value = "reviewCursorList", allEntries = true),
            @CacheEvict(value = "counselorReviewList", allEntries = true),
            @CacheEvict(value = "myReviewList", allEntries = true)
    })
    @Transactional
    public ReviewIdResponse createReview(
            ReviewCreateRequest request, Member member
    ) {

        Counsel counsel = counselRepository.getCounselByMember(member);

        Review newReview = createAndSaveReview(request, member, counsel);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = request.getImageUrls().stream()
                    .map(key -> s3Service.confirmReviewImage(key, member.getId(), newReview.getId(), List.of()))
                    .distinct()
                    .map(url -> reviewMapper.toReviewImage(newReview, url))
                    .toList();

            reviewImageRepository.saveAll(images);
        }

        return new ReviewIdResponse(newReview.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "reviewList", allEntries = true),
            @CacheEvict(value = "reviewCursorList", allEntries = true),
            @CacheEvict(value = "counselorReviewList", allEntries = true),
            @CacheEvict(value = "myReviewList", allEntries = true)
    })
    @Transactional
    public ReviewIdResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long memberId) {

        // 수정 권한 유효성 검사(본인이 아닌 경우 수정 불가)
        Review review = reviewRepository.getReviewForUpdate(reviewId);
        ParamValidator.validModify(review.getMember().getId(), memberId);

        review.updateReviewInfo(request);

        List<String> remainKeys = Optional.ofNullable(request.getRemainImageUrls())
                .orElseGet(ArrayList::new);

        List<String> addKeys = Optional.ofNullable(request.getAddImageUrls())
                .orElseGet(ArrayList::new);

        List<String> existingKeys = review.getImages().stream().map(ReviewImage::getUrl).toList();
        s3Service.validateRetainedReviewImages(remainKeys, existingKeys);
        List<String> confirmedKeys = addKeys.stream()
                .map(key -> s3Service.confirmReviewImage(key, memberId, reviewId, existingKeys))
                .distinct()
                .toList();
        // An already attached image passed in addImageUrls means retain, not delete and recreate.
        remainKeys = new ArrayList<>(remainKeys);
        remainKeys.addAll(confirmedKeys.stream().filter(existingKeys::contains).toList());
        addKeys = confirmedKeys.stream().filter(key -> !existingKeys.contains(key)).toList();

        List<String> deleteKeys = review.updateImages(remainKeys, addKeys);

        if (!deleteKeys.isEmpty()) {
            s3Service.deleteFilesAfterCommit(deleteKeys);
        }

        return new  ReviewIdResponse(review.getId());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "reviewList", allEntries = true),
            @CacheEvict(value = "reviewCursorList", allEntries = true),
            @CacheEvict(value = "counselorReviewList", allEntries = true),
            @CacheEvict(value = "myReviewList", allEntries = true)
    })
    @Transactional
    public ReviewIdResponse deleteReview(Long reviewId, Long memberId){

        // 수정 권한 유효성 검사(본인이 아닌 경우 수정 불가)
        Review review = reviewRepository.getReviewForUpdate(reviewId);
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
    @Cacheable(
            value = "reviewList",
            key = "'member=' + (#member == null ? 'anonymous' : #member.id) + ':sort=' + #sortType.name() + ':page=' + #page + ':size=' + #size",
            condition = "#page == 0"
    )
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
    @Cacheable(
            value = "reviewCursorList",
            key = "'member=' + (#member == null ? 'anonymous' : #member.id) + ':size=' + #size",
            condition = "#cursor == null"
    )
    @Transactional(readOnly = true)
    public ReviewCursorResponse<ReviewSummaryResponse> inquiryReviewsByCursor(Member member, String cursor, int size) {
        if (size < 1 || size > MAX_CURSOR_PAGE_SIZE) {
            throw new CustomApiException(ErrorCode.INVALID_REVIEW_PAGE_SIZE);
        }

        // 1단계: 인덱스에서 (작성 시각, ID)만 한 건 더 읽어 다음 페이지가 있는지 판단한다(전체 개수를 세지 않음)
        Pageable limit = PageRequest.of(0, size + 1);
        List<ReviewCursor> keys;
        if (cursor == null) {
            keys = reviewRepository.findLatestKeys(limit);
        } else {
            ReviewCursor after = ReviewCursor.decode(cursor);
            keys = reviewRepository.findLatestKeysAfter(after.createdAt(), after.id(), limit);
        }

        boolean hasNext = keys.size() > size;
        List<ReviewCursor> pageKeys = hasNext ? keys.subList(0, size) : keys;
        String nextCursor = hasNext ? pageKeys.get(pageKeys.size() - 1).encode() : null;

        // 2단계: 고른 리뷰만 회원·상담과 함께 조회하고, 1단계 순서대로 다시 정렬한다
        List<Long> ids = pageKeys.stream().map(ReviewCursor::id).toList();
        Map<Long, Review> byId = ids.isEmpty() ? Map.of()
                : reviewRepository.findAllWithMemberAndCounselByIdIn(ids).stream()
                        .collect(Collectors.toMap(Review::getId, Function.identity()));

        List<ReviewSummaryResponse> reviews = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(review -> reviewMapper.toReviewSummaryResponse(review, member))
                .toList();

        return ReviewCursorResponse.<ReviewSummaryResponse>builder()
                .reviews(reviews)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Override
    @Cacheable(
            value = "counselorReviewList",
            key = "'member=' + #member.id + ':counselor=' + #counselorId + ':sort=' + #sortType.name() + ':page=' + #page + ':size=' + #size",
            condition = "#page == 0"
    )
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
