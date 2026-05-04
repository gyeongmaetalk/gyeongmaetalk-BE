package auctionTalk.auction.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentProvider {

    REVENUECAT("RevenueCat");

    private final String description;

}
