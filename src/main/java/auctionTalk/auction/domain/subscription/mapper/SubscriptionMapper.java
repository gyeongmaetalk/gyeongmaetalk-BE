package auctionTalk.auction.domain.subscription.mapper;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPagingResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPreparePaymentResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
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
                    .subscriptionStatus(SubscriptionStatus.PENDING)
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

    public SubscriptionResponse toSubscriptionResponse(Subscription subscription){
        Member member = subscription.getMember();

        return SubscriptionResponse.builder()
                .subscriptionId(subscription.getId())
                .memberId(member.getId())
                .memberName(member.getName())
                .memberCellPhone(member.getCellPhone())
                .startTime(subscription.getStartDate())
                .subscriptionStatus(subscription.getSubscriptionStatus())
                .build();
    }

    public <T>SubscriptionPagingResponse<T> toSubscriptionPagingResponse(Page<T> subscriptions){
        return SubscriptionPagingResponse.<T>builder()
                .subscriptions(subscriptions.getContent())
                .page(subscriptions.getNumber())
                .totalPages(subscriptions.getTotalPages())
                .totalElements((int) subscriptions.getTotalElements())
                .isFirst(subscriptions.isFirst())
                .isLast(subscriptions.isLast())
                .build();
    }
}
