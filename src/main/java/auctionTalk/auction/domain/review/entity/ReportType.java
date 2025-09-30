package auctionTalk.auction.domain.review.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    ABUSE("욕설 및 비방"),
    ADVERTISEMENT("허위/광고 게시물"),
    PERSONAL_INFO("개인 정보 포함"),
    ETC("기타");

    private final String description;
}
