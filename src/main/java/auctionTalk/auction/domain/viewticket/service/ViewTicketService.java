package auctionTalk.auction.domain.viewticket.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketPurchasePageResponse;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketWalletResponse;

public interface ViewTicketService {

    ViewTicketPurchasePageResponse getPurchasePage(Member member);
    ViewTicketWalletResponse getMyWallet(Member member);
    void grant(Order order, ProductComponent component, Integer quantity);
}
