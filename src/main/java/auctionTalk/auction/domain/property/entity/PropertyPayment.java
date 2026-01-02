package auctionTalk.auction.domain.property.entity;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PropertyPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Property property;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public void updatePaymentStatus(PaymentStatus status){
        this.status = status;
    }
}