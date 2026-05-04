package auctionTalk.auction.domain.viewticket.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketPurchasePageResponse;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketWalletResponse;

public interface ViewTicketService {

    ViewTicketPurchasePageResponse getPurchasePage(Long memberId);
    ViewTicketWalletResponse getMyWallet(Long memberId);
    void grant(Order order, ProductComponent component, Integer quantity);
}
