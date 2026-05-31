package auctionTalk.auction.domain.payment.dto.response;

import auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto.RevenueCatNonSubscriptionPurchase;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto.RevenueCatSubscriber;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueCatCustomerResponse {

    private RevenueCatSubscriber subscriber;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RevenueCatSubscriber {

        @JsonProperty("non_subscriptions")
        private Map<String, List<RevenueCatNonSubscriptionPurchase>> nonSubscriptions;

        @JsonProperty("entitlements")
        private Map<String, RevenueCatEntitlement> entitlements;
    }
}