package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPreparePaymentResponse;

public interface SubscriptionService {

    SubscriptionPreparePaymentResponse prepareSubscriptionPayment(Member member, Long counselorId);
    PaymentResultResponse confirmSubscriptionPayment(Long subscriptionId, PaymentConfirmRequest paymentConfirmRequest);
    SubscriptionIdResponse completeSubscription(Long subscriptionId);
}
