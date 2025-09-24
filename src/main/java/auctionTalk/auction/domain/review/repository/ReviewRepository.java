package auctionTalk.auction.domain.review.repository;

import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    default Review getReview(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Query("SELECT r FROM Review r " +
            "JOIN r.counsel c " +
            "JOIN c.counselor co " +
            "WHERE co.id = :counselorId " +
            "ORDER BY r.createdAt DESC")
    Page<Review> findLatestByCounselorId(@Param("counselorId") Long counselorId, Pageable pageable);

    // 상담사 ID + 오래된순
    @Query("SELECT r FROM Review r " +
            "JOIN r.counsel c " +
            "JOIN c.counselor co " +
            "WHERE co.id = :counselorId " +
            "ORDER BY r.createdAt ASC")
    Page<Review> findOldestByCounselorId(@Param("counselorId") Long counselorId, Pageable pageable);

    // 상담사 ID + 별점 높은순
    @Query("SELECT r FROM Review r " +
            "JOIN r.counsel c " +
            "JOIN c.counselor co " +
            "WHERE co.id = :counselorId " +
            "ORDER BY r.score DESC")
    Page<Review> findHighestRatingByCounselorId(@Param("counselorId") Long counselorId, Pageable pageable);

    // 상담사 ID + 별점 낮은순
    @Query("SELECT r FROM Review r " +
            "JOIN r.counsel c " +
            "JOIN c.counselor co " +
            "WHERE co.id = :counselorId " +
            "ORDER BY r.score ASC")
    Page<Review> findLowestRatingByCounselorId(@Param("counselorId") Long counselorId, Pageable pageable);
}