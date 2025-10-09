package auctionTalk.auction.domain.subscription.mapper;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    public Subscription toSubscription(Member member,  Counselor counselor){
            return Subscription.builder()
                    .member(member)
                    .counselor(counselor)
                    .status(SubscriptionStatus.PENDING)
                    .build();
    }
}
