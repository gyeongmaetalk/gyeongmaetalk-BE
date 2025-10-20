package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;

public interface SubscriptionService {

    SubscriptionIdResponse prepareSubscriptionPayment(Member member, Long counselorId);
    SubscriptionIdResponse activateSubscription(Long subscriptionId);
    SubscriptionIdResponse completeSubscription(Long subscriptionId);
}
