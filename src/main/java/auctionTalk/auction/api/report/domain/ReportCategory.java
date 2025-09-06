package auctionTalk.auction.api.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportCategory {
    ABUSE("욕설 및 비방"),
    ADVERTISING("허위/광고성 게시물"),
    PERSONAL_INFO("개인정보 노출");

    private final String toKorean;
}
