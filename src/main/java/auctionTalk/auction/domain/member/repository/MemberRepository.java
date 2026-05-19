package auctionTalk.auction.domain.member.repository;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
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
        SELECT *
        FROM member
        WHERE client_id = :clientId
          AND login_type = :#{#loginType.name()}
        ORDER BY 
          CASE WHEN deleted_at IS NULL THEN 0 ELSE 1 END,
          id DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Member> findByClientIdAndLoginTypeIncludingDeleted(
            @Param("clientId") String clientId,
            @Param("loginType") LoginType loginType
    );

    Optional<Member> findByUsername(String username);
    Optional<Member> findByClientIdAndLoginType(String clientId, LoginType loginType);
    Optional<Member> findByUsernameAndLoginType(String username, LoginType loginType);
}