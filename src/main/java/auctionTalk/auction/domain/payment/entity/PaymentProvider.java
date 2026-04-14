package auctionTalk.auction.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentProvider {

    APPLE("애플 인앱 결제"),
    GOOGLE("구글 인앱 결제");

    private final String description;

}
