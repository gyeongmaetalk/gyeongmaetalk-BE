package auctionTalk.auction.domain.viewticket.repository;

import auctionTalk.auction.domain.viewticket.entity.MemberViewTicketWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberViewTicketWalletRepository extends JpaRepository<MemberViewTicketWallet, Long> {

    Optional<MemberViewTicketWallet> findByMemberId(Long memberId);
}
