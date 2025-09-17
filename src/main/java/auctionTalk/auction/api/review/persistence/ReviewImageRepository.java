package auctionTalk.auction.api.review.persistence;

import auctionTalk.auction.api.review.domain.Review;
import auctionTalk.auction.api.review.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReview(Review review);
}
