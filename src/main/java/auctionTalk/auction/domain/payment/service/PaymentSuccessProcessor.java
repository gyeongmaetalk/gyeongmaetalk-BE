package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.RevenueCatVerifiedPurchase;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentSuccessProcessor {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentFulfillmentService paymentFulfillmentService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentConfirmResponse processSuccess(
            Long orderId,
            Long paymentId,
            RevenueCatVerifiedPurchase verifiedPurchase
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isSuccess() && order.isSuccess()) {
            return paymentMapper.toPaymentConfirmResponse(order, payment);
        }

        payment.markSuccessByRevenueCat(
                verifiedPurchase.getTransactionIdentifier(),
                verifiedPurchase.getProductIdentifier(),
                verifiedPurchase.getStore(),
                verifiedPurchase.getSandbox(),
                verifiedPurchase.getPurchasedAt()
        );

        order.markSuccess();

        paymentFulfillmentService.fulfill(order);

        return paymentMapper.toPaymentConfirmResponse(order, payment);
    }
}