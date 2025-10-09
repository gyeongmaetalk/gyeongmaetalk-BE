package auctionTalk.auction.domain.counsel.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CounselStatus {
    NONE("상담 예약 없음"),
    COUNSEL_AFTER(" 상담 전"),
    COUNSEL_BEFORE("상담 후"),
    SUBSCRIBE("추천 매물 구독 중");

    private final String description;
}
