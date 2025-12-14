package auctionTalk.auction.domain.subscription.dto.response;

import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionResponse {

    private Long subscriptionId;

    private String memberName;

    private String memberCellPhone;

    private SubscriptionStatus subscriptionStatus;
}
