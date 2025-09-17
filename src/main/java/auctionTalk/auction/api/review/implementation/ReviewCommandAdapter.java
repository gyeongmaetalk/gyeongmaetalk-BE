package auctionTalk.auction.api.review.implementation;

import auctionTalk.auction.api.counsel.domain.Counsel;
import auctionTalk.auction.api.review.domain.Review;
import auctionTalk.auction.api.review.persistence.ReviewRepository;
import auctionTalk.auction.api.review.presentation.dto.ReviewDto;
import auctionTalk.auction.api.user.domain.User;
import auctionTalk.auction.global.annotations.Adapter;
import auctionTalk.auction.global.common.exception.ThrowClass.ReviewException;
import auctionTalk.auction.global.common.exception.base.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Adapter
@RequiredArgsConstructor
public class ReviewCommandAdapter {

    private final ReviewRepository reviewRepository;
    private final ReviewImageCommandAdapter reviewImageCommandAdapter;

    public Review createReview(ReviewDto.ReviewCreateRequestDto requestDto, User user, Counsel counsel) {

        Review review = Review.builder()
                .user(user)
                .counsel(counsel)
                .score(requestDto.getScore())
                .content(requestDto.getContent())
                .build();

        return reviewRepository.save(review);
    }

    public Review updateReview(Long reviewId, ReviewDto.ReviewUpdateRequestDto requestDto, List<MultipartFile> newImages) {

        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new ReviewException(GlobalErrorCode.REVIEW_NOT_FOUND)
        );

        review.updateReviewInfo(requestDto);

        // 이미지 업데이트
        if (newImages != null && !newImages.isEmpty()) {
            reviewImageCommandAdapter.updateReviewImages(review, requestDto.getExistingImages(), newImages);
        }

        return review;
    }
}
