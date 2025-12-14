package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.service.PaymentService;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPreparePaymentResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.mapper.SubscriptionMapper;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
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
    private final PaymentService paymentService;

    @Value("${subscribe.fixed-amount}")
    private Long fixedAmount;

    @Value("${subscribe.fixed-name}")
    private String fixedName;

    @Override
    @Transactional
    public SubscriptionPreparePaymentResponse prepareSubscriptionPayment(Member member, Long counselorId){

        Long amount = this.fixedAmount;
        String orderName = this.fixedName;

        String orderId = generateUniqueOrderId(member.getId());

        Counselor counselor = counselorRepository.getCounselor(counselorId);

        Subscription subscription = subscriptionMapper.toSubscription(member, counselor, orderId, amount, orderName);
        subscriptionRepository.save(subscription);
        return subscriptionMapper.toSubscriptionPreparePaymentResponse(subscription);
    }

    private String generateUniqueOrderId(Long memberId) {
        return "SUB-" + memberId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public PaymentResultResponse confirmSubscriptionPayment(Long subscriptionId, PaymentConfirmRequest paymentConfirmRequest) {

        PaymentResultResponse response = paymentService.callTossPaymentApi(paymentConfirmRequest);

        Subscription subscription = subscriptionRepository.getSubscription(subscriptionId);

        switch (response.getStatus()) {
            case "DONE":
                subscription.activate(response.getPaymentKey());
                break;
            case "CANCELED": // 토스 취소
                subscription.failed();
                break;

            case "ABORTED": // 결제 실패
                subscription.failed();
                break;
            case "EXPIRED": // 결제 만료
                subscription.failed();
                break;

            default:
                throw new CustomApiException(ErrorCode.FAIL_CONFIRM_PAYMENT);
        }

        subscriptionRepository.save(subscription);

        return response;
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
