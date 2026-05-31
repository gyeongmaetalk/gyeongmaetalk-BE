package auctionTalk.auction.domain.order.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    READY("결제 대기"),
    COMPLETED("주문 완료"),
    FAILED("주문 실패"),
    REFUND("주문 환불");

    private final String description;
}