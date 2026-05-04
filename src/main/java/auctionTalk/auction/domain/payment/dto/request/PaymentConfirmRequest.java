package auctionTalk.auction.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "인앱 결제 승인 요청")
public class PaymentConfirmRequest {

    @Schema(description = "주문 ID", example = "1")
    @NotNull
    private Long orderId;

    @Schema(description = "RevenueCat 상품 식별자", example = "view_ticket_10")
    @NotBlank
    private String productIdentifier;

    @Schema(description = "RevenueCat 결제 transactionIdentifier", example = "1000001234567890")
    @NotBlank
    private String transactionIdentifier;
}