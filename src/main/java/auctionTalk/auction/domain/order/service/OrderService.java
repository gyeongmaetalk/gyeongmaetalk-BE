package auctionTalk.auction.domain.order.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.dto.request.OrderCreateRequest;
import auctionTalk.auction.domain.order.dto.response.OrderCreateResponse;

public interface OrderService {

    OrderCreateResponse createOrder(Member member, OrderCreateRequest request);
}
