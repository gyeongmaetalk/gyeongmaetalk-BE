package auctionTalk.auction.domain.payment.dto.response;

import auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto.RevenueCatSubscriber;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueCatCustomerResponse {

    private RevenueCatSubscriber subscriber;
}