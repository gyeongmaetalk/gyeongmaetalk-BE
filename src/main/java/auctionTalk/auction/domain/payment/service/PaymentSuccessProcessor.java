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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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
        String step = "INIT";

        try {
            step = "LOAD_ORDER";
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new CustomApiException(ErrorCode.ORDER_NOT_FOUND));

            step = "LOCK_PAYMENT";
            Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                    .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));

            log.info("[PAYMENT_SUCCESS_LOCKED] orderId={}, paymentId={}, paymentStatus={}, orderStatus={}",
                    orderId, paymentId, payment.getPaymentStatus(), order.getOrderStatus());

            if (payment.isSuccess() && order.isSuccess()) {
                log.info("[PAYMENT_SUCCESS_ALREADY_PROCESSED] orderId={}, paymentId={}", orderId, paymentId);
                return paymentMapper.toPaymentConfirmResponse(order, payment);
            }

            step = "MARK_PAYMENT_SUCCESS";
            payment.markSuccessByRevenueCat(
                    verifiedPurchase.getTransactionIdentifier(),
                    verifiedPurchase.getProductIdentifier(),
                    verifiedPurchase.getStore(),
                    verifiedPurchase.getSandbox(),
                    verifiedPurchase.getPurchasedAt()
            );

            step = "MARK_ORDER_SUCCESS";
            order.markSuccess();

            step = "FULFILL_PAYMENT";
            paymentFulfillmentService.fulfill(order,payment);

            step = "CREATE_RESPONSE";
            PaymentConfirmResponse response = paymentMapper.toPaymentConfirmResponse(order, payment);

            log.info("[PAYMENT_SUCCESS_COMPLETED] orderId={}, paymentId={}, transactionIdentifier={}",
                    orderId,
                    paymentId,
                    verifiedPurchase.getTransactionIdentifier());

            return response;

        } catch (Exception e) {
            log.error("[PAYMENT_SUCCESS_PROCESS_FAILED] step={}, orderId={}, paymentId={}",
                    step, orderId, paymentId, e);
            throw e;
        }
    }
}