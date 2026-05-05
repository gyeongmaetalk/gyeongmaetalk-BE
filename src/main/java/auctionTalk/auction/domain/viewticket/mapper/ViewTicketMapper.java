package auctionTalk.auction.domain.viewticket.mapper;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketPurchasePageResponse;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketWalletResponse;
import auctionTalk.auction.domain.viewticket.entity.MemberViewTicketWallet;
import auctionTalk.auction.domain.viewticket.entity.ViewTicketGrantHistory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewTicketMapper {

    public MemberViewTicketWallet toMemberViewTicketWallet(Member member) {
        return MemberViewTicketWallet.builder()
                .member(member)
                .balance(0)
                .build();
    }

    public ViewTicketWalletResponse toViewTicketWalletResponse(String packageName, Integer balance) {
        return ViewTicketWalletResponse.builder()
                .packageName(packageName)
                .balance(balance)
                .build();
    }

    public ViewTicketGrantHistory toViewTicketGrantHistory(Order order, ProductComponent component, Integer quantity) {
        return ViewTicketGrantHistory.builder()
                .order(order)
                .productComponent(component)
                .quantity(quantity)
                .build();
    }

    public ViewTicketPurchasePageResponse toViewTicketPurchasePageResponse(Integer balance, List<ProductDetailResponse> products){
        return ViewTicketPurchasePageResponse.builder()
                .balance(balance)
                .products(products)
                .build();
    }
}
