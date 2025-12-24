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

    Optional<Counsel> findByMember(Member member);

    @Query("SELECT c.counselTime FROM Counsel c " +
            "WHERE c.counselor.id = :counselorId " +
            "AND c.counselDate = :date")
    List<LocalTime> findReservedTimes(@Param("counselorId") Long counselorId,
                                      @Param("date") LocalDate date);

    @Query("SELECT c FROM Counsel c " +
            "WHERE c.pushSent = false " +
            "AND FUNCTION('TIMESTAMP', c.counselDate, c.counselTime) <= :threshold")
    List<Counsel> findAllForPush(@Param("threshold") LocalDateTime threshold);

    List<Counsel> findAllByCounselStatusIn(List<CounselStatus> statuses);

    @Query("""
        select c
        from Counsel c
        where c.counselStatus in (:statuses)
          and (c.counselDate < :today
               or (c.counselDate = :today and c.counselTime <= :nowTime))
    """)
    List<Counsel> findExpiredCounsels(
            @Param("status") CounselStatus status,
            @Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime
    );
}