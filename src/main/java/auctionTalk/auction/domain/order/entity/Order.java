package auctionTalk.auction.domain.order.entity;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.entity.ProductType;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_member_id", columnList = "member_id"),
                @Index(name = "idx_orders_product_id", columnList = "product_id"),
                @Index(name = "idx_orders_order_number", columnList = "order_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_order_number", columnNames = "order_number")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String idempotencyKey;

    private String productCode;

    private String productName;

    private Long counselorId;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Long amount;

    public boolean isSuccess() {
        return this.orderStatus == OrderStatus.COMPLETED;
    }

    public void markSuccess() {
        if (this.orderStatus == OrderStatus.COMPLETED) {
            return;
        }

        if (this.orderStatus != OrderStatus.READY) {
            throw new IllegalStateException("완료 처리 가능한 주문 상태가 아닙니다.");
        }

        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void markFail() {
        if (this.orderStatus == OrderStatus.FAILED) {
            return;
        }

        if (this.orderStatus != OrderStatus.READY) {
            throw new IllegalStateException("실패 처리 가능한 주문 상태가 아닙니다.");
        }

        this.orderStatus = OrderStatus.FAILED;
    }

    public void markRefund() {
        if (this.orderStatus == OrderStatus.REFUND) {
            return;
        }

        if (this.orderStatus != OrderStatus.COMPLETED) {
            throw new IllegalStateException("환불 처리 가능한 주문 상태가 아닙니다.");
        }

        this.orderStatus = OrderStatus.REFUND;
    }

}
