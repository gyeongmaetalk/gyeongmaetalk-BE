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
                @UniqueConstraint(name = "uk_payments_idempotency_key", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uk_payments_provider_transaction_id", columnNames = {"payment_provider", "provider_transaction_id"})
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
     * 결제사 기준 거래 식별자
     * - APPLE: transactionId
     * - GOOGLE: orderId 또는 latestSuccessfulOrderId
     */
    private String providerTransactionId;

    /**
     * Google 전용 검증 키
     * - GOOGLE: purchaseToken
     * - APPLE: null
     */
    private String purchaseToken;

    /**
     * 실제 결제 검증에 사용된 스토어 상품 ID 스냅샷
     * - APPLE: productId
     * - GOOGLE: productId
     */
    private String storeProductId;

    /**
     * 원본 검증 데이터
     * - APPLE: signedTransactionInfo JWS
     * - GOOGLE: purchaseToken 또는 raw response json
     */
    @Lob
    private String rawProviderPayload;

    private Long requestedAmount;

    private Long approvedAmount;

    private LocalDateTime approvedAt;

    private LocalDateTime failedAt;

    private String failureReason;

    private String failureCode;

    private LocalDateTime refundedAt;

    /**
     * Google acknowledge 완료 시각
     * - APPLE은 null
     */
    private LocalDateTime acknowledgedAt;

    public void markVerifying(PaymentProvider paymentProvider, String storeProductId, String purchaseToken) {
        this.paymentProvider = paymentProvider;
        this.storeProductId = storeProductId;
        this.purchaseToken = purchaseToken;
        this.paymentStatus = PaymentStatus.VERIFYING;
    }


    public void markApproved(
            PaymentProvider paymentProvider,
            String providerTransactionId,
            String purchaseToken,
            String storeProductId,
            String rawProviderPayload,
            Long approvedAmount,
            LocalDateTime approvedAt
    ) {
        this.paymentProvider = paymentProvider;
        this.providerTransactionId = providerTransactionId;
        this.purchaseToken = purchaseToken;
        this.storeProductId = storeProductId;
        this.rawProviderPayload = rawProviderPayload;
        this.approvedAmount = approvedAmount;
        this.approvedAt = approvedAt;
        this.paymentStatus = PaymentStatus.SUCCESS;

        this.failedAt = null;
        this.failureCode = null;
        this.failureReason = null;
    }

    public void markFailed(
            PaymentProvider paymentProvider,
            String purchaseToken,
            String storeProductId,
            String rawProviderPayload,
            String failureCode,
            String failureReason,
            LocalDateTime failedAt
    ) {
        this.paymentProvider = paymentProvider;
        this.purchaseToken = purchaseToken;
        this.storeProductId = storeProductId;
        this.rawProviderPayload = rawProviderPayload;
        this.paymentStatus = PaymentStatus.FAIL;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.failedAt = failedAt;
    }

    public void markRefunded(LocalDateTime refundedAt) {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundedAt = refundedAt;
    }

    public void markAcknowledged(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public boolean isSuccess() {
        return this.paymentStatus == PaymentStatus.SUCCESS;
    }
}
