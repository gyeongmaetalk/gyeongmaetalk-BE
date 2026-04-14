package auctionTalk.auction.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    READY("결제 승인 대기"),
    SUCCESS("결제 성공"),
    FAIL("결제 승인 실패"),
    CANCELED("결제 취소"),
    REFUNDED("결제 환불");

    private final String description;
}