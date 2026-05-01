package auctionTalk.auction.domain.viewticket.mapper;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.viewticket.entity.MemberViewTicketWallet;
import auctionTalk.auction.domain.viewticket.entity.ViewTicketGrantHistory;
import org.springframework.stereotype.Component;

@Component
public class ViewTicketMapper {

    public MemberViewTicketWallet toMemberViewTicketWallet(Member member) {
        return MemberViewTicketWallet.builder()
                .member(member)
                .balance(0)
                .build();
    }

    public ViewTicketGrantHistory toViewTicketGrantHistory(Order order, ProductComponent component, Integer quantity) {
        return ViewTicketGrantHistory.builder()
                .order(order)
                .productComponent(component)
                .quantity(quantity)
                .build();
    }
}
