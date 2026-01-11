package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPagingResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.mapper.SubscriptionMapper;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionServiceImpl implements AdminSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final CounselRepository counselRepository;

    public SubscriptionPagingResponse<SubscriptionResponse> inquirySubscriptions(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "startDate") // 최신순
        );

        Page<Subscription> subscriptions =  subscriptionRepository.findAllByOrderByCreatedAtDesc(pageable);

        Page<SubscriptionResponse> subscriptionResponsePage = subscriptions.map(subscriptionMapper::toSubscriptionResponse);

        return subscriptionMapper.toSubscriptionPagingResponse(subscriptionResponsePage);
    }

    @Override
    @Transactional
    public SubscriptionIdResponse updateSubscriptionStatus(Long memberId, Long subscriptionId, PaymentStatus status) {

        Subscription subscription = subscriptionRepository.getSubscription(subscriptionId);
        Counsel counsel = counselRepository.getCounselByMemberId(memberId);

        if(status == PaymentStatus.SUCCESS) {
            counsel.updateStatus(CounselStatus.SUBSCRIBE);
            subscription.activate();
        }
        if(status == PaymentStatus.FAIL) {
            counsel.updateStatus(CounselStatus.COUNSEL_AFTER);
            subscription.failed();
        }

        subscriptionRepository.save(subscription);

        return new SubscriptionIdResponse(subscriptionId);
    }

}
