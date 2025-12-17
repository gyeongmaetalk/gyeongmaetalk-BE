package auctionTalk.auction.domain.subscription.dto.response;

import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionResponse {

    private Long subscriptionId;

    private Long memberId;

    private String memberName;

    private String memberCellPhone;

    private LocalDateTime startTime;

    private SubscriptionStatus subscriptionStatus;

}
