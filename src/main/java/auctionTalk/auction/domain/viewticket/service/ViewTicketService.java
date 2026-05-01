package auctionTalk.auction.domain.viewticket.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.entity.ProductComponent;

public interface ViewTicketService {

    void grant(Order order, ProductComponent component, Integer quantity);
}
