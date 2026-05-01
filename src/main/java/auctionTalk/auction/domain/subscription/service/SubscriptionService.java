package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;

public interface SubscriptionService {

    void createFromPaidOrder(Order order);

      //legacy
//    SubscriptionIdResponse prepareSubscriptionPayment(Member member, Long counselorId);
//    SubscriptionIdResponse completeSubscription(Long subscriptionId);
}
