package auctionTalk.auction.domain.subscription.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {
    PENDING("결제 승인 대기"),
    IN_PROGRESS("구독중"),
    COMPLETED("완료됨"),
    CANCELED("취소됨"),
    PAYMENT_FAILED("결제 실패");

    private final String description;
}
