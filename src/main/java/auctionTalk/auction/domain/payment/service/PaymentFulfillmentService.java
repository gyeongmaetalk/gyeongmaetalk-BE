package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.order.entity.Order;

public interface PaymentFulfillmentService {

    void fulfill(Order order);
}
