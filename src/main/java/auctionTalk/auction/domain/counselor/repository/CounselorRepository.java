package auctionTalk.auction.domain.counselor.repository;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CounselorRepository extends JpaRepository<Counselor, Long> {

    default Counselor getCounselor(Long counselorId) {
        return findById(counselorId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSELOR_NOT_FOUND));
    }

    default Counselor getCounselorWithLock(Long counselorId) {
        return findByIdWithLock(counselorId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSELOR_NOT_FOUND));
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Counselor c where c.id = :counselorId")
    Optional<Counselor> findByIdWithLock(Long counselorId);
}
