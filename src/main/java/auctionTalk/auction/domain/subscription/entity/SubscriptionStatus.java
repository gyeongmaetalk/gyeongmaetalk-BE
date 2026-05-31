package auctionTalk.auction.domain.subscription.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {
    IN_PROGRESS("구독중"),
    COMPLETED("완료됨"),
    CANCELED("취소됨");
    private final String description;
}
