package auctionTalk.auction.domain.subscription.service;

import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPagingResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionResponse;

public interface AdminSubscriptionService {
    SubscriptionPagingResponse<SubscriptionResponse> inquirySubscriptions(int page, int size);
    SubscriptionIdResponse updateSubscriptionStatus(Long memberId, Long subscriptionId, PaymentStatus status);

}
