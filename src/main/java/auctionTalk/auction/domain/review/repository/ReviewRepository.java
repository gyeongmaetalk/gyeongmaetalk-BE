package auctionTalk.auction.domain.review.repository;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCursor;
import auctionTalk.auction.domain.review.dto.response.ReviewStats;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findForUpdate(@Param("id") Long id);

    default Review getReviewForUpdate(Long id) {
        return findForUpdate(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.REVIEW_NOT_FOUND));
    }

    default Review getReview(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.REVIEW_NOT_FOUND));
    }

    // 상담사 평점 표시용. 리뷰 엔티티를 전부 불러와 자바에서 세지 않고 DB에서 개수·평균만 계산한다.
    @Query("SELECT new auctionTalk.auction.domain.review.dto.response.ReviewStats(COUNT(r), AVG(r.score)) " +
            "FROM Review r WHERE r.counsel.counselor = :counselor")
    ReviewStats getStatsByCounselor(@Param("counselor") Counselor counselor);

    boolean existsByCounsel(Counsel counsel);

    Page<Review> findReviewsByMember(Member member, Pageable pageable);

    // 최신순 커서 조회 1단계(첫 페이지): (작성 시각, ID)만 읽는다.
    // review 테이블만 보므로 idx_review_created_at을 역순으로 읽고 끝난다(커버링 인덱스, 정렬 없음).
    // 회원·상담을 여기서 조인하면 옵티마이저가 회원 테이블부터 읽고 전체를 정렬할 수 있어 분리했다.
    // List 반환이라 COUNT 쿼리를 실행하지 않는다.
    @Query("SELECT new auctionTalk.auction.domain.review.dto.request.ReviewCursor(r.createdAt, r.id) " +
            "FROM Review r " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewCursor> findLatestKeys(Pageable pageable);

    // 최신순 커서 조회 1단계(다음 페이지): 커서보다 앞선 (작성 시각, ID)만 읽는다.
    @Query("SELECT new auctionTalk.auction.domain.review.dto.request.ReviewCursor(r.createdAt, r.id) " +
            "FROM Review r " +
            "WHERE r.createdAt < :createdAt " +
            "OR (r.createdAt = :createdAt AND r.id < :id) " +
            "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReviewCursor> findLatestKeysAfter(@Param("createdAt") LocalDateTime createdAt,
                                           @Param("id") Long id,
                                           Pageable pageable);

    // 최신순 커서 조회 2단계: 1단계에서 고른 리뷰만 회원·상담과 함께 가져온다.
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.member " +
            "JOIN FETCH r.counsel " +
            "WHERE r.id IN :ids")
    List<Review> findAllWithMemberAndCounselByIdIn(@Param("ids") List<Long> ids);

    // 전체 리뷰 + 최신순
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    Page<Review> findLatest(Pageable pageable);

    // 전체 리뷰 + 오래된순
    @Query("SELECT r FROM Review r ORDER BY r.createdAt ASC")
    Page<Review> findOldest(Pageable pageable);

    // 전체 리뷰 + 별점 높은순
    @Query("SELECT r FROM Review r ORDER BY r.score DESC")
    Page<Review> findHighestRating(Pageable pageable);

    // 전체 리뷰 + 별점 낮은순
    @Query("SELECT r FROM Review r ORDER BY r.score ASC")
    Page<Review> findLowestRating(Pageable pageable);

    // 상담사 ID + 최신순
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
