package auctionTalk.auction.domain.counselor.repository;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselorRepository extends JpaRepository<Counselor, Long> {

    default Counselor getCounselor(Long counselorId) {
        return findById(counselorId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSELOR_NOT_FOUND));
    }
}
