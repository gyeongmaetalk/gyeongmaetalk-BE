package auctionTalk.auction.domain.review.repository;

import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReview(Review review);
}