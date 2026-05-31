package auctionTalk.auction.domain.member.repository;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = """
        SELECT id
        FROM member
        WHERE client_id = :clientId
          AND login_type = :loginType
        ORDER BY id ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Long> findIdByClientIdAndLoginTypeIncludingDeleted(
            @Param("clientId") String clientId,
            @Param("loginType") String loginType
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE member
        SET deleted_at = NULL
        WHERE id = :memberId
        """, nativeQuery = true)
    int restoreById(@Param("memberId") Long memberId);

    Optional<Member> findByUsername(String username);
    Optional<Member> findByClientIdAndLoginType(String clientId, LoginType loginType);
    Optional<Member> findByUsernameAndLoginType(String username, LoginType loginType);
}