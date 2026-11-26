package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
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

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentSuccessProcessor {

    private final PaymentRepository paymentRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSuccess(
            Long orderId,
            Long paymentId,
            PaymentVerificationResult result
    ) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));

        entityManager.refresh(payment);

        Order order = payment.getOrder();
        entityManager.refresh(order, LockModeType.PESSIMISTIC_WRITE);

        if (!Objects.equals(orderId, order.getId())) {
            throw new CustomApiException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (result == null
                || result.getProvider() != payment.getPaymentProvider()
                || result.getProviderTransactionId() == null
                || result.getProviderTransactionId().isBlank()) {
            throw new CustomApiException(ErrorCode.FAIL_CONFIRM_PAYMENT);
        }

        if (!Objects.equals(
                payment.getStoreProductId(),
                result.getStoreProductId()
        )) {
            throw new CustomApiException(
                    ErrorCode.PRODUCT_IDENTIFIER_MISMATCH
            );
        }

        // 이미 동일한 결제 성공이 기록되어 있다면
        // 지급 단계만 다시 시도할 수 있도록 정상 종료한다.
        if (payment.isSuccess()) {
            if (!order.isSuccess()
                    || !Objects.equals(
                    payment.getProviderTransactionId(),
                    result.getProviderTransactionId()
            )) {
                throw new CustomApiException(
                        ErrorCode.DUPLICATED_PAYMENT_TRANSACTION
                );
            }

            return;
        }

        if (order.isSuccess()) {
            throw new CustomApiException(
                    ErrorCode.FAIL_CONFIRM_PAYMENT
            );
        }

        paymentRepository
                .findByPaymentProviderAndProviderTransactionId(
                        result.getProvider(),
                        result.getProviderTransactionId()
                )
                .filter(existing ->
                        !existing.getId().equals(paymentId)
                )
                .ifPresent(existing -> {
                    throw new CustomApiException(
                            ErrorCode.DUPLICATED_PAYMENT_TRANSACTION
                    );
                });

        payment.markSuccess(
                result.getProvider(),
                result.getProviderTransactionId(),
                result.getStoreProductId(),
                result.getStore(),
                result.getSandbox(),
                result.getApprovedAt(),
                result.getApprovedAmount()
        );

        order.markSuccess();

        // DB unique 제약 위반을 성공 기록 commit 전에 확인한다.
        paymentRepository.flush();
    }
}