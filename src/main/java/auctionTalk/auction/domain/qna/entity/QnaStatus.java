package auctionTalk.auction.domain.qna.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum QnaStatus {
    PENDING("답변 대기"),
    ANSWERED("답변 완료");

    private final String description;
}
