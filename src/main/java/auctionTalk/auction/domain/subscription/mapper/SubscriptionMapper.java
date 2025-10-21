package auctionTalk.auction.domain.subscription.mapper;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPreparePaymentResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public Subscription toSubscription(Member member,  Counselor counselor, String orderId, Long amount, String orderName){
            return Subscription.builder()
                    .member(member)
                    .counselor(counselor)
                    .orderId(orderId)
                    .amount(amount)
                    .orderName(orderName)
                    .status(SubscriptionStatus.PENDING)
                    .build();
    }

    public SubscriptionPreparePaymentResponse toSubscriptionPreparePaymentResponse(Subscription subscription){
        return SubscriptionPreparePaymentResponse.builder()
                .subscriptionId(subscription.getId())
                .orderId(subscription.getOrderId())
                .amount(subscription.getAmount())
                .orderName(subscription.getOrderName())
                .build();
    }
}
