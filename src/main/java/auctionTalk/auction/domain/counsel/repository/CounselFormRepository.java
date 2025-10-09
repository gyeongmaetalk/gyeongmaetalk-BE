package auctionTalk.auction.domain.counsel.repository;

import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CounselFormRepository extends JpaRepository<CounselForm, Long> {

    default CounselForm getCounselFormById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSEL_NOT_FOUND));
    }

    default CounselForm getCounselFormByMember(Member member) {
        return findByMember(member)
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSEL_FORM_NOT_FOUND));
    }

    Optional<CounselForm> findByMember(Member member);
}
