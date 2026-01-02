package auctionTalk.auction.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제 승인 대기"),
    SUCCESS("결제 성공"),
    FAIL("결제 승인 실패");

    private final String description;
}