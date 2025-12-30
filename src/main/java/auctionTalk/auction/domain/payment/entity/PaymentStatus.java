package auctionTalk.auction.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제 준비"),
    SUCCESS("결제 성공"),
    FAIL("결제 실패");

    private final String description;
}