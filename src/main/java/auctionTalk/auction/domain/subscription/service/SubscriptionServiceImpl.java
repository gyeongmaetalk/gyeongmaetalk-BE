package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.payment.entity.Payment;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CounselorRepository counselorRepository;
    private final SubscriptionMapper subscriptionMapper;


    @Override
    @Transactional
    public void createFromPaidOrder(Order order, Payment payment) {
        if (subscriptionRepository.existsBySourceOrderId(order.getId())) {
            return;
        }

        Member member = order.getMember();

        Counselor counselor = counselorRepository.findById(order.getCounselorId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.COUNSELOR_NOT_FOUND));

        if (subscriptionRepository.existsByMemberIdAndSubscriptionStatus(
                member.getId(),
                SubscriptionStatus.IN_PROGRESS
        )) {
            throw new CustomApiException(ErrorCode.SUBSCRIPTION_ALREADY_PENDING);
        }

        subscriptionRepository.save(
                subscriptionMapper.toSubscription(order.getId(), member, counselor, payment)
        );
    }

      //legacy
//    @Override
//    @Transactional
//    public SubscriptionIdResponse prepareSubscriptionPayment(Member member, Long counselorId){
//
//        if (subscriptionRepository.existsByMemberAndSubscriptionStatus(
//                member, SubscriptionStatus.PENDING)) {
//            throw new CustomApiException(ErrorCode.SUBSCRIPTION_ALREADY_PENDING);
//        }
//
//        Counselor counselor = counselorRepository.getCounselor(counselorId);
//        Subscription subscription = subscriptionRepository
//                .findByMember(member)
//                .orElseGet(() -> subscriptionMapper.toSubscription(member, counselor));
//        subscription.initStatus();
//
//        subscriptionRepository.save(subscription);
//        return new SubscriptionIdResponse(subscription.getId());
//    }
//
//    private String generateUniqueOrderId(Long memberId) {
//        return "SUB-" + memberId + "-" + UUID.randomUUID().toString().substring(0, 8);
//    }
//
//    @Override
//    @Transactional
//    public SubscriptionIdResponse completeSubscription(Long subscriptionId) {
//        Subscription subscription = subscriptionRepository.getSubscription(subscriptionId);
//
//        subscription.complete();
//
//        subscriptionRepository.save(subscription);
//
//        return new SubscriptionIdResponse(subscription.getId());
//    }

}
