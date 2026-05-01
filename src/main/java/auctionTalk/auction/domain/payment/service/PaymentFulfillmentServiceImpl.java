package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.product.entity.ProductComponentMapping;
import auctionTalk.auction.domain.subscription.service.SubscriptionService;
import auctionTalk.auction.domain.viewticket.service.ViewTicketService;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentFulfillmentServiceImpl implements PaymentFulfillmentService{

    private final ViewTicketService viewTicketService;
    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public void fulfill(Order order) {
        Product product = order.getProduct();

        for (ProductComponentMapping mapping : product.getComponentMappings()) {
            ProductComponent component = mapping.getComponent();
            Integer quantity = mapping.getQuantity();

            switch (component.getComponentType()) {
                case VIEW_TICKET -> viewTicketService.grant(order, component, quantity);
                case SUBSCRIPTION -> subscriptionService.createFromPaidOrder(order);
                default -> throw new CustomApiException(ErrorCode.INVALID_COMPONENT_TYPE);
            }
        }
    }
}
