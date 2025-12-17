package auctionTalk.auction.domain.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {

    private String paymentKey;
    private String cancelReason;
    private Long cancelAmount;
}