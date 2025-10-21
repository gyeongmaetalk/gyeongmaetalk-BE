package auctionTalk.auction.domain.subscription.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPreparePaymentResponse {

    @Schema(description = "추천 매물 구독 아이디", example = "1")
    private Long subscriptionId;

    @Schema(description = "주문 아이디", example = "SUB-1-*SE&")
    private String orderId;

    @Schema(description = "결제금액", example = "300000")
    private Long amount;

    @Schema(description = "주문 이름", example = "경매 대행 신청")
    private String orderName;
}
