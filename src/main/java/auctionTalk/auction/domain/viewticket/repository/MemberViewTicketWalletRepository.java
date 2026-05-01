package auctionTalk.auction.domain.viewticket.repository;

import auctionTalk.auction.domain.viewticket.entity.MemberViewTicketWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberViewTicketWalletRepository extends JpaRepository<MemberViewTicketWallet, Long> {

    Optional<MemberViewTicketWallet> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from MemberViewTicketWallet w where w.member.id = :memberId")
    Optional<MemberViewTicketWallet> findByMemberIdForUpdate(@Param("memberId") Long memberId);
}
