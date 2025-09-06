package auctionTalk.auction.api.subscribe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscribeStatus {
    ACTIVE("활성화", "현재 구독이 유효한 상태"),
    COMPLETE("완료됨", "추천 매물 낙찰이 완료된 상태"),
    PENDING("대기중", "결제 대기 또는 승인 대기 상태");

    private final String description;
    private final String detail;
}
