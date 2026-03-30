package auctionTalk.auction.domain.review.repository;

import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.entity.ReviewImage;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReview(Review review);

    @Query("""
        select ri
        from ReviewImage ri
        where ri.review is null
          and ri.createdAt < :cutoff
    """)
    List<ReviewImage> findUnusedImagesBefore(@Param("cutoff") LocalDateTime cutoff);
}