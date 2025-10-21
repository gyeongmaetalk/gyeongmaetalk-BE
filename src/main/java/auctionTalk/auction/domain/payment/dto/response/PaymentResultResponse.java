package auctionTalk.auction.domain.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResultResponse {

    private String status;
    private String paymentKey;
    private String orderId;
    private Long totalAmount;
    private String failureReason;
}
