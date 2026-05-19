package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.payment.entity.Payment;

public interface PaymentFulfillmentService {

    void fulfill(Order order, Payment payment);
}
