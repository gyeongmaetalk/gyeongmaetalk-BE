package auctionTalk.auction.api.review.implementation;

import auctionTalk.auction.api.review.business.ReviewMapper;
import auctionTalk.auction.api.review.domain.Review;
import auctionTalk.auction.api.review.domain.ReviewSortType;
import auctionTalk.auction.api.review.persistence.ReviewRepository;
import auctionTalk.auction.api.review.presentation.dto.ReviewDto;
import auctionTalk.auction.api.user.domain.User;
import auctionTalk.auction.global.annotations.Adapter;
import auctionTalk.auction.global.common.exception.ThrowClass.ReviewException;
import auctionTalk.auction.global.common.exception.base.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Adapter
@RequiredArgsConstructor
public class ReviewQueryAdapter {

    private final ReviewRepository reviewRepository;

    private final ReviewMapper reviewMapper;

    public Review inquiryReview(Long reviewId) {

        return reviewRepository.findById(reviewId).orElseThrow(
                () -> new ReviewException(GlobalErrorCode.REVIEW_NOT_FOUND)
        );
    }

    public Page<ReviewDto.ReviewSummaryResponseDto> inquiryReviews(Long counselorId, User user, ReviewSortType sortType, Pageable pageable) {

        Page<Review> reviewPage = switch (sortType) {
            case LATEST -> reviewRepository.findLatestByCounselorId(counselorId, pageable);
            case OLDEST -> reviewRepository.findOldestByCounselorId(counselorId, pageable);
            case HIGHEST_SCORE ->  reviewRepository.findHighestRatingByCounselorId(counselorId, pageable);
            case LOWEST_SCORE ->  reviewRepository.findLowestRatingByCounselorId(counselorId, pageable);
            default ->  throw new ReviewException(GlobalErrorCode.INVALID_REVIEW_SORT_TYPE);
        };

        return reviewPage.map(review -> reviewMapper.toReviewSummaryResponseDto(review, user));
    }
}
