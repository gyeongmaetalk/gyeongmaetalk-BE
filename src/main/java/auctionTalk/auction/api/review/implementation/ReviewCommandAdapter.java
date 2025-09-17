package auctionTalk.auction.api.review.implementation;

import auctionTalk.auction.api.counsel.domain.Counsel;
import auctionTalk.auction.api.review.domain.Review;
import auctionTalk.auction.api.review.persistence.ReviewRepository;
import auctionTalk.auction.api.review.presentation.dto.ReviewDto;
import auctionTalk.auction.api.user.domain.User;
import auctionTalk.auction.global.annotations.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class ReviewCommandAdapter {

    private final ReviewRepository reviewRepository;

    public Review createReview(ReviewDto.ReviewCreateRequestDto requestDto, User user, Counsel counsel) {

        Review review = Review.builder()
                .user(user)
                .counsel(counsel)
                .score(requestDto.getScore())
                .content(requestDto.getContent())
                .build();

        return reviewRepository.save(review);
    }

}
