package auctionTalk.auction.domain.subscription.mapper;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPagingResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public Subscription toSubscription(Long sourceOrderId, Member member, Counselor counselor, Payment payment){
            return Subscription.builder()
                    .sourceOrderId(sourceOrderId)
                    .member(member)
                    .counselor(counselor)
                    .subscriptionStatus(SubscriptionStatus.IN_PROGRESS)
                    .payment(payment)
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
