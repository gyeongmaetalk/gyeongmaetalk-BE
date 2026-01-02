package auctionTalk.auction.domain.payment.dto.response;

import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResultResponse {

    private PaymentStatus status;
}
