package auctionTalk.auction.domain.counsel.repository;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CounselRepository extends JpaRepository<Counsel, Long> {

    default Counsel getCounselById(Long counselId) {
        return findById(counselId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));
    }

    default Counsel getCounselByMember(Member member) {
        return findByMember(member)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));
    }

    default Counsel getCounselByMemberId(Long memberId) {
        return findByMemberId(memberId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));
    }

    Optional<Counsel> findByMember(Member member);
    Optional<Counsel> findByMemberId(Long memberId);
    Optional<Counsel> findTopByMemberIdAndCounselorIdOrderByIdDesc(
            Long memberId,
            Long counselorId
    );


    @Query("SELECT c.counselTime FROM Counsel c " +
            "WHERE c.counselor.id = :counselorId " +
            "AND c.counselDate = :date")
    List<LocalTime> findReservedTimes(@Param("counselorId") Long counselorId,
                                      @Param("date") LocalDate date);

    default List<Counsel> findAllForPush(LocalDateTime threshold) {
        return findPushTargets(threshold.toLocalDate(), threshold.toLocalTime());
    }

    // 컬럼을 TIMESTAMP() 함수로 감싸면 인덱스를 못 쓰므로 날짜·시간 조건으로 풀어서 비교한다(idx_counsel_push).
    // 발송 대상마다 회원을 따로 조회하지 않도록 회원을 함께 가져온다.
    @Query("SELECT c FROM Counsel c JOIN FETCH c.member " +
            "WHERE c.pushSent = false " +
            "AND (c.counselDate < :date OR (c.counselDate = :date AND c.counselTime <= :time))")
    List<Counsel> findPushTargets(@Param("date") LocalDate date, @Param("time") LocalTime time);

    List<Counsel> findAllByCounselStatusIn(List<CounselStatus> statuses);

    @Query("""
        select c
        from Counsel c
        where c.counselStatus = :status
          and (c.counselDate < :today
               or (c.counselDate = :today and c.counselTime <= :nowTime))
    """)
    List<Counsel> findExpiredCounsels(
            @Param("status") CounselStatus status,
            @Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime
    );

    boolean existsByCounselorIdAndCounselDateAndCounselTime(
            Long counselorId,
            LocalDate counselDate,
            LocalTime counselTime
    );
}