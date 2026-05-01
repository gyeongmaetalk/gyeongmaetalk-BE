package auctionTalk.auction.domain.order.repository;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByMemberIdAndIdempotencyKey(Long memberId, String idempotencyKey);

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select o
        from Order o
        join fetch o.product
        where o.id = :orderId
          and o.member.id = :memberId
    """)
    Optional<Order> findForUpdateByIdAndMemberId(@Param("orderId") Long orderId,
                                                 @Param("memberId") Long memberId);
}
