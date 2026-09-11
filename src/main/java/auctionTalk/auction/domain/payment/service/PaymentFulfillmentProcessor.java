package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFulfillmentProcessor {

    private final PaymentRepository paymentRepository;
    private final PaymentFulfillmentService paymentFulfillmentService;
    private final PaymentMapper paymentMapper;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentConfirmResponse fulfill(Long paymentId) {

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() ->
                        new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND)
                );

        entityManager.refresh(payment);

        Order order = payment.getOrder();
        entityManager.refresh(
                order,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (!payment.isSuccess() || !order.isSuccess()) {
            throw new CustomApiException(
                    ErrorCode.FAIL_CONFIRM_PAYMENT
            );
        }

        // 이미 상품 지급까지 완료됐다면 그대로 성공 응답
        if (payment.isFulfilled()) {
            return paymentMapper.toPaymentConfirmResponse(
                    order,
                    payment
            );
        }

        // 동일 회원의 서로 다른 주문이 동시에 지급돼도
        // 티켓 수량 등의 갱신 손실이 발생하지 않도록 잠근다.
        entityManager.refresh(
                order.getMember(),
                LockModeType.PESSIMISTIC_WRITE
        );

        paymentFulfillmentService.fulfill(order, payment);

        payment.markFulfillmentSuccess();

        return paymentMapper.toPaymentConfirmResponse(
                order,
                payment
        );
    }
}