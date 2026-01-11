package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.payment.service.PaymentService;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import auctionTalk.auction.domain.subscription.mapper.SubscriptionMapper;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CounselRepository counselRepository;
    private final CounselorRepository counselorRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public SubscriptionIdResponse prepareSubscriptionPayment(Member member, Long counselorId){

        if (subscriptionRepository.existsByMemberAndSubscriptionStatus(
                member, SubscriptionStatus.PENDING)) {
            throw new CustomApiException(ErrorCode.SUBSCRIPTION_ALREADY_PENDING);
        }

        Counselor counselor = counselorRepository.getCounselor(counselorId);
        Subscription subscription = subscriptionRepository
                .findByMember(member)
                .orElseGet(() -> subscriptionMapper.toSubscription(member, counselor));
        subscription.initStatus();

        subscriptionRepository.save(subscription);
        return new SubscriptionIdResponse(subscription.getId());
    }

    private String generateUniqueOrderId(Long memberId) {
        return "SUB-" + memberId + "-" + UUID.randomUUID().toString().substring(0, 8);
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
