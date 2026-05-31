package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RevenueCatVerifiedPurchase {

    private String productIdentifier;
    private String transactionIdentifier;
    private String store;
    private Boolean sandbox;
    private LocalDateTime purchasedAt;
}