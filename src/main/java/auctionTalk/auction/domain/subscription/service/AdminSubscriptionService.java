package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPagingResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionResponse;

public interface AdminSubscriptionService {
    SubscriptionPagingResponse<SubscriptionResponse> inquirySubscriptions(int page, int size);
}
