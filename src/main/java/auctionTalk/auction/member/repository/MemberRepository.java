package auctionTalk.auction.member.repository;

import auctionTalk.api.domain.member.entity.LoginType;
import auctionTalk.api.domain.member.entity.Member;
import auctionTalk.api.global.exception.CustomApiException;
import auctionTalk.api.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getMember(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    Optional<Member> findByClientIdAndLoginType(String clientId, LoginType loginType);
}