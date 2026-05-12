package auctionTalk.auction.domain.order.mapper;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.dto.request.OrderCreateRequest;
import auctionTalk.auction.domain.order.dto.response.OrderCreateResponse;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.entity.OrderStatus;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toOrder(Member member, Product product, String orderNumber, String idempotencyKey, Long counselorId){
        return Order.builder()
                .member(member)
                .product(product)
                .orderNumber(orderNumber)
                .idempotencyKey(idempotencyKey)
                .orderStatus(OrderStatus.FAILED)
                .amount(product.getPrice())
                .counselorId(counselorId)
                .build();
    }

    public OrderCreateResponse toCreateResponse(Order order, Payment payment) {
        Product product = order.getProduct();

        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .productId(product.getId())
                .productName(product.getName())
                .amount(order.getAmount())
                .storeProductId(payment.getStoreProductId())
                .revenueCatAppUserId(order.getMember().getRevenueCatAppUserId())
                .build();
    }
}