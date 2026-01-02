package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;

public interface SubscriptionService {

    SubscriptionIdResponse prepareSubscriptionPayment(Member member, Long counselorId);
    SubscriptionIdResponse updateSubscriptionStatus(Member member, Long subscriptionId, PaymentStatus status);
    SubscriptionIdResponse completeSubscription(Long subscriptionId);
}
