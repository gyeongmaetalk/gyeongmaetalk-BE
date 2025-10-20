package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.mapper.SubscriptionMapper;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CounselorRepository counselorRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Value("${subscribe.fixed-amount}")
    private Long fixedAmount;

    @Value("${subscribe.fixed-name}")
    private String fixedName;

    @Override
    @Transactional
    public SubscriptionIdResponse prepareSubscriptionPayment(Member member, Long counselorId){

        Long amount = this.fixedAmount;
        String orderName = this.fixedName;

        String orderId = generateUniqueOrderId(member.getId());

        Counselor counselor = counselorRepository.getCounselor(counselorId);

        Subscription subscription = subscriptionMapper.toSubscription(member, counselor, orderId, amount, orderName);
        subscriptionRepository.save(subscription);
        return new SubscriptionIdResponse(subscription.getId());
    }

    private String generateUniqueOrderId(Long memberId) {
        return "SUB-" + memberId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public SubscriptionIdResponse activateSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.getSubscription(subscriptionId);

        subscription.activate();

        subscriptionRepository.save(subscription);

        return new SubscriptionIdResponse(subscription.getId());
    }

    @Override
    @Transactional
    public SubscriptionIdResponse completeSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.getSubscription(subscriptionId);

        subscription.complete();

        subscriptionRepository.save(subscription);

        return new SubscriptionIdResponse(subscription.getId());
    }
}
