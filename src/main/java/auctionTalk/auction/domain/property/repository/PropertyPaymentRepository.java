package auctionTalk.auction.domain.property.repository;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyPaymentRepository extends JpaRepository<PropertyPayment, Long> {
    Optional<PropertyPayment> findByOrderId(String orderId);
    boolean existsByMemberAndPropertyIdAndStatus(Member member, Long propertyId, PaymentStatus status);

    Page<PropertyPayment> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
