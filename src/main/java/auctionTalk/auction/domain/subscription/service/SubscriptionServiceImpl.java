package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.mapper.SubscriptionMapper;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CounselorRepository counselorRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public SubscriptionIdResponse createSubscription(Member member, Long counselorId){

        Counselor counselor = counselorRepository.getCounselor(counselorId);

        Subscription subscription = subscriptionMapper.toSubscription(member, counselor);
        subscriptionRepository.save(subscription);
        return new SubscriptionIdResponse(subscription.getId());
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
