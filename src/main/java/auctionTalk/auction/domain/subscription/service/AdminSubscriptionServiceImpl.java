package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPagingResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.mapper.SubscriptionMapper;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import kotlinx.serialization.Required;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSubscriptionServiceImpl implements AdminSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

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
}
