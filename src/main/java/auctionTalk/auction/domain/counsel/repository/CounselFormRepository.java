package auctionTalk.auction.domain.counsel.repository;

import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CounselFormRepository extends JpaRepository<CounselForm, Long> {

    default CounselForm getCounselFormById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));
    }
}
