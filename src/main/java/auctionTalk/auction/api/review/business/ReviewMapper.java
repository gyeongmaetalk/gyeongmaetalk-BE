package auctionTalk.auction.api.review.business;

import auctionTalk.auction.api.review.domain.Review;
import auctionTalk.auction.api.review.domain.ReviewImage;
import auctionTalk.auction.api.review.presentation.dto.ReviewDto;
import auctionTalk.auction.api.user.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;

import java.util.Objects;

@Component
public class ReviewMapper {

    public ReviewImage toReviewImage(Review review, String url) {
        return ReviewImage.builder()
                .review(review)
                .url(url)
                .build();
    }

    public ReviewDto.ReviewSummaryResponseDto toReviewSummaryResponseDto(Review review, User user) {
        return ReviewDto.ReviewSummaryResponseDto.builder()
                .reviewId(review.getId())
                .score(review.getScore())
                .name(review.getUser().getName())
                .createAt(review.getCreatedAt())
                .counselDateTime(review.getCounsel().getBookedDate())
                .isMine(Objects.equals(review.getUser().getId(), user.getId()))
                .imageCount(review.getImages().size())
                .thumbnail(review.getThumbnail())
                .content(review.getContent())
                .build();
    }

    public <T> ReviewDto.ReviewPagingResponseDto<T> toReviewPagingResponseDto(Page<T> reviews) {
        return ReviewDto.ReviewPagingResponseDto.<T>builder()
                .reviews(reviews.getContent())
                .page(reviews.getNumber())
                .totalPages(reviews.getTotalPages())
                .totalElements((int) reviews.getTotalElements())
                .isFirst(reviews.isFirst())
                .isLast(reviews.isLast())
                .build();
    }
}
