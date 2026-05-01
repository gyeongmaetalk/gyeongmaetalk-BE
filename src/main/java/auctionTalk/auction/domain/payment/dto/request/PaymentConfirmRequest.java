package auctionTalk.auction.domain.payment.dto.request;

import auctionTalk.auction.domain.payment.entity.PaymentProvider;
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

    @Schema(description = "결제 제공자", example = "GOOGLE", allowableValues = {"GOOGLE", "APPLE"})
    @NotNull
    private PaymentProvider provider;

    @Schema(description = "스토어 상품 ID", example = "view_ticket_10")
    @NotBlank
    private String storeProductId;

    @Schema(
            description = "스토어 검증 데이터. GOOGLE은 purchaseToken, APPLE은 signedTransactionInfo(JWS)",
            example = "sample-verification-data"
    )
    @NotBlank
    private String providerVerificationData;
}