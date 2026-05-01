package auctionTalk.auction.domain.viewticket.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.viewticket.entity.MemberViewTicketWallet;
import auctionTalk.auction.domain.viewticket.entity.ViewTicketGrantHistory;
import auctionTalk.auction.domain.viewticket.mapper.ViewTicketMapper;
import auctionTalk.auction.domain.viewticket.repository.MemberViewTicketWalletRepository;
import auctionTalk.auction.domain.viewticket.repository.ViewTicketGrantHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewTicketServiceImpl implements ViewTicketService{

    private final MemberViewTicketWalletRepository memberViewTicketWalletRepository;
    private final ViewTicketGrantHistoryRepository viewTicketGrantHistoryRepository;
    private final ViewTicketMapper viewTicketMapper;

    @Override
    @Transactional
    public void grant(Order order, ProductComponent component, Integer quantity) {
        validateQuantity(quantity);

        if (viewTicketGrantHistoryRepository.existsByOrderIdAndProductComponentId(order.getId(), component.getId())) {
            return;
        }

        Member member = order.getMember();

        MemberViewTicketWallet wallet = memberViewTicketWalletRepository.findByMemberId(member.getId())
                .orElseGet(() -> memberViewTicketWalletRepository.save(new MemberViewTicketWallet(member, 0)));

        wallet.increase(quantity);

        viewTicketGrantHistoryRepository.save(
                viewTicketMapper.toViewTicketGrantHistory(order, component, quantity)
        );
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("열람권 수량은 1 이상이어야 합니다.");
        }
    }
}
