package auctionTalk.auction.domain.counsel.repository;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CounselRepository extends JpaRepository<Counsel, Long> {

    default Counsel getCounselByMember(Member member) {
        return findByMember(member)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));
    }

    Optional<Counsel> findByMember(Member member);

    @Query("SELECT c.counselTime FROM Counsel c " +
            "WHERE c.counselor.id = :counselorId " +
            "AND C.counselDate = :date")
    List<LocalTime> findReservedTimes(@Param("counselorId") Long counselorId,
                                      @Param("date") LocalDate date);
}