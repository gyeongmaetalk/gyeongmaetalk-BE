package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFailureRecorder {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long orderId, Long paymentId, String failureCode, String failureReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));

        payment.markFailed(failureCode, failureReason);
        order.markFail();
    }
}