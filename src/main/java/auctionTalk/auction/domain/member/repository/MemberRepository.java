package auctionTalk.auction.domain.member.repository;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getMember(Long id) {
        Member member= findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getDeletedAt() != null) {
            throw new CustomApiException(ErrorCode.MEMBER_SOFT_DELETE);
        }

        return member;
    }

    Optional<Member> findByClientIdAndLoginType(String clientId, LoginType loginType);
}