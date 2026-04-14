package auctionTalk.auction.domain.payment.entity;

import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentNumber;

    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String providerTransactionId;

    private String originalProviderTransactionId;

    private String providerPurchaseToken;

    private Long requestedAmount;

    private Long approvedAmount;

    private LocalDateTime approvedAt;

    private LocalDateTime failedAt;

    private LocalDateTime canceledAt;

    private String failureReason;

    private LocalDateTime refundedAt;

    private String failureCode;

    private String failureMessage;
}
