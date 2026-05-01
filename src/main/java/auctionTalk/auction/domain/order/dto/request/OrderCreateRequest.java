package auctionTalk.auction.domain.order.dto.request;

import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @Schema(description = "상품 ID", example = "1")
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @Schema(description = "결제 수단", example = "APPLE")
    @NotNull
    private PaymentProvider paymentProvider;

    @Schema(description = "주문 생성 멱등키", example = "8f6f2e1c-9f17-4c66-a4b1-8c8d1d234567")
    @NotBlank
    private String idempotencyKey;

    @Schema(description = "경매대행 신청할 상담사 ID", example = "1")
    private Long counselorId;
}