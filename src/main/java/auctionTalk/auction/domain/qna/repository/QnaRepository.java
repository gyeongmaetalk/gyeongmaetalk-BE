package auctionTalk.auction.domain.qna.repository;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.qna.entity.Qna;
import auctionTalk.auction.domain.qna.entity.QnaStatus;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    default Qna getQna(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.QNA_NOT_FOUND));
    }

    @Query("""
        select q
        from Qna q
        where (:startAt is null or q.createdAt >= :startAt)
          and (:endAt is null or q.createdAt <= :endAt)
    """)
    Page<Qna> findAllByCreatedAtBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );
    List<Qna> findByMember(Member member);

    @Query("""
        SELECT q
        FROM Qna q
        WHERE (:startAt IS NULL OR q.createdAt >= :startAt)
          AND (:endAt IS NULL OR q.createdAt <= :endAt)
          AND (:status IS NULL OR q.status = :status)
    """)
    Page<Qna> findAllByCondition(
            @Param("status") QnaStatus status,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            Pageable pageable
    );
}
