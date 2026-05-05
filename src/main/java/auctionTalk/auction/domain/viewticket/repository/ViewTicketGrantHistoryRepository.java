package auctionTalk.auction.domain.viewticket.repository;

import auctionTalk.auction.domain.viewticket.entity.ViewTicketGrantHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ViewTicketGrantHistoryRepository extends JpaRepository<ViewTicketGrantHistory, Long> {

    boolean existsByOrderIdAndProductComponentId(Long orderId, Long productComponentId);

    @Query("""
        select p.name
        from ViewTicketGrantHistory h
        join h.order o
        join o.product p
        join h.productComponent pc
        where o.member.id = :memberId
          and pc.componentType = :componentType
        order by h.createdAt desc
    """)
    List<String> findLatestPackageNameByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}