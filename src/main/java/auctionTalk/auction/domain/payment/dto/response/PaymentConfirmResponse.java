package auctionTalk.auction.domain.payment.dto.response;

import auctionTalk.auction.domain.order.entity.OrderStatus;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentConfirmResponse {

    @Schema(description = "주문 ID", example = "10")
    private Long orderId;

    @Schema(description = "주문 번호", example = "ORD-1713351123123")
    private String orderNumber;

    @Schema(description = "결제 번호", example = "PAY-1713351123981")
    private String paymentNumber;

    @Schema(description = "내부 상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품명", example = "경매 대행 영구 이용권")
    private String productName;

    @Schema(description = "주문 상태", example = "COMPLETED")
    private OrderStatus orderStatus;

    @Schema(description = "결제 상태", example = "SUCCESS")
    private PaymentStatus paymentStatus;

    @Schema(description = "승인 금액", example = "300000")
    private Long approvedAmount;

    @Schema(description = "결제 승인 시각", example = "2026-04-18T15:30:00")
    private LocalDateTime approvedAt;
}