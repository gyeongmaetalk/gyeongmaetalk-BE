package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.payment.entity.PaymentProvider;

/** Provider 입력은 서버에서 조회한 주문/결제 정보로 구성한다. */
public record PaymentVerificationCommand(
        Long memberId,
        Long orderId,
        PaymentProvider provider,
        String productIdentifier,
        String transactionIdentifier,
        Long requestedAmount
) {
}
