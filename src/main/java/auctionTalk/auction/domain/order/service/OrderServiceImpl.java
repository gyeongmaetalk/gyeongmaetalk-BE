package auctionTalk.auction.domain.order.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.dto.request.OrderCreateRequest;
import auctionTalk.auction.domain.order.dto.response.OrderCreateResponse;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.mapper.OrderMapper;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.domain.payment.service.verify.PaymentConfirmService;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.repository.ProductRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentConfirmService paymentConfirmService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    @Override
    @Transactional
    public OrderCreateResponse createOrder(Member member, OrderCreateRequest request){
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.PRODUCT_NOT_FOUND));


        //기존 주문 항목이 있으면 그대로 반환
        Order existingOrder = orderRepository
                .findByMemberIdAndIdempotencyKey(member.getId(), request.getIdempotencyKey())
                .orElse(null);

        if (existingOrder != null) {
            Payment existingPayment = paymentRepository.findByOrderId(existingOrder.getId())
                    .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));
            return orderMapper.toCreateResponse(existingOrder, existingPayment);
        }

        String orderNumber = generate();
        Order order = orderMapper.toOrder(member, product, orderNumber, request.getIdempotencyKey(), request.getPaymentProvider(), request.getCounselorId());
        orderRepository.save(order);

        String paymentNumber = paymentConfirmService.generate();
        String storeProductId = product.getStoreProductId();

        Payment payment = paymentMapper.toPayment(order, paymentNumber, storeProductId);
        paymentRepository.save(payment);

        return orderMapper.toCreateResponse(order, payment);
    }


    private String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "ORD-" + timestamp + "-" + random;
    }
}
