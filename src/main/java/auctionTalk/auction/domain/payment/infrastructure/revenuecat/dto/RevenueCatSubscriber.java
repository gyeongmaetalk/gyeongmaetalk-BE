package auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueCatSubscriber {

    @JsonProperty("non_subscriptions")
    private Map<String, List<RevenueCatNonSubscriptionPurchase>> nonSubscriptions = new HashMap<>();
}