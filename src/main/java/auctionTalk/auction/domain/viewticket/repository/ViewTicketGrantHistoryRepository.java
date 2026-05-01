package auctionTalk.auction.domain.viewticket.repository;

import auctionTalk.auction.domain.viewticket.entity.ViewTicketGrantHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewTicketGrantHistoryRepository extends JpaRepository<ViewTicketGrantHistory, Long> {

    boolean existsByOrderIdAndProductComponentId(Long orderId, Long productComponentId);
}