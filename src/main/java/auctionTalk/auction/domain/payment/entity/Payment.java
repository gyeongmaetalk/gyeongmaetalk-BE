package auctionTalk.auction.domain.payment.entity;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_order_id", columnList = "order_id"),
                @Index(name = "idx_payments_provider_transaction_id", columnList = "payment_provider, provider_transaction_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_payment_number", columnNames = "payment_number"),
                @UniqueConstraint(
                        name = "uk_payments_provider_transaction_id",
                        columnNames = {"payment_provider", "provider_transaction_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    private String paymentNumber;

    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;


    /**
     * RevenueCat 또는 스토어 거래 식별자
     * transactionIdentifier 저장
     */
    private String providerTransactionId;

    /**
     * 결제된 스토어 상품 ID
     * 예: view_ticket_10
     */
    private String storeProductId;


    private Long requestedAmount;

    private Long approvedAmount;

    private LocalDateTime approvedAt;

    private LocalDateTime failedAt;

    private String failureReason;

    private String failureCode;

    private String store;

    private Boolean sandbox = false;

    /**
     * Google acknowledge 완료 시각
     * - APPLE은 null
     */
    private LocalDateTime acknowledgedAt;

    public void markSuccessByRevenueCat(
            String transactionIdentifier,
            String storeProductId,
            String store,
            Boolean sandbox,
            LocalDateTime approvedAt
    ) {
        this.paymentProvider = PaymentProvider.REVENUECAT;
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.providerTransactionId = transactionIdentifier;
        this.storeProductId = storeProductId;
        this.store = store;
        this.sandbox = sandbox;
        this.approvedAt = approvedAt != null ? approvedAt : LocalDateTime.now();
    }

    public void markFailed(String failureCode, String failureReason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureCode = failureCode;
        this.failureReason = failureReason;
    }


    public boolean isSuccess() {
        return this.paymentStatus == PaymentStatus.SUCCESS;
    }
}
