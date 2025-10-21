package auctionTalk.auction.domain.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmRequest {

    private String paymentKey;
    private String orderId;
    private Long amount;
}
