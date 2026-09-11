package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.domain.payment.service.PaymentFulfillmentProcessor;
import auctionTalk.auction.domain.payment.service.PaymentSuccessProcessor;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmServiceImpl implements PaymentConfirmService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentVerificationServiceResolver verificationServiceResolver;
    private final PaymentSuccessProcessor paymentSuccessProcessor;
    private final PaymentFulfillmentProcessor paymentFulfillmentProcessor;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentConfirmResponse confirm(
            Long memberId,
            PaymentConfirmRequest request
    ) {
        try {
            TransactionTemplate read = createReadTransaction();

            Confirmation confirmation =
                    read.execute(status -> prepare(memberId, request));

            if (confirmation == null) {
                throw new CustomApiException(ErrorCode.FAIL_CONFIRM_PAYMENT);
            }

            // 결제 + 지급까지 이미 완료된 동일 요청
            if (confirmation.response() != null) {
                return confirmation.response();
            }

            // 결제는 성공했지만 지급만 실패했던 경우
            // 외부 결제 검증을 다시 하지 않고 지급만 재시도한다.
            if (confirmation.command() == null) {
                return paymentFulfillmentProcessor.fulfill(
                        confirmation.paymentId()
                );
            }

            PaymentVerificationCommand command = confirmation.command();

            // 외부 Provider 검증은 DB 트랜잭션 밖에서 수행한다.
            PaymentVerificationResult result =
                    verificationServiceResolver
                            .resolve(command.provider())
                            .verify(command);

            try {
                // TX 1
                // 결제 성공 사실과 주문 성공 상태를 먼저 확정한다.
                paymentSuccessProcessor.processSuccess(
                        command.orderId(),
                        confirmation.paymentId(),
                        result
                );

            } catch (DataIntegrityViolationException e) {

                /*
                 * 동시에 같은 provider transactionId가 처리된 경우
                 * DB unique constraint가 최종 방어선 역할을 한다.
                 *
                 * transaction이 rollback 된 이후 다시 조회하여
                 * 실제 transactionId 중복인 경우에만
                 * DUPLICATED_PAYMENT_TRANSACTION으로 변환한다.
                 */
                boolean duplicate = Boolean.TRUE.equals(
                        read.execute(status ->
                                paymentRepository
                                        .findByPaymentProviderAndProviderTransactionId(
                                                result.getProvider(),
                                                result.getProviderTransactionId()
                                        )
                                        .filter(payment ->
                                                !payment.getId()
                                                        .equals(confirmation.paymentId())
                                        )
                                        .isPresent()
                        )
                );

                if (duplicate) {
                    throw new CustomApiException(
                            ErrorCode.DUPLICATED_PAYMENT_TRANSACTION
                    );
                }

                throw e;
            }

            /*
             * TX 2
             *
             * Payment SUCCESS / Order SUCCESS는 이미 commit된 상태다.
             * 상품 지급이 실패하면 fulfillment만 rollback되고
             * PaymentFulfillmentStatus는 PENDING으로 남는다.
             */
            return paymentFulfillmentProcessor.fulfill(
                    confirmation.paymentId()
            );

        } catch (CustomApiException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "[PAYMENT_CONFIRM_FAILED] memberId={}, orderId={}",
                    memberId,
                    request.getOrderId(),
                    e
            );

            throw new CustomApiException(
                    ErrorCode.FAIL_CONFIRM_PAYMENT
            );
        }
    }

    private Confirmation prepare(
            Long memberId,
            PaymentConfirmRequest request
    ) {
        Order order = orderRepository
                .findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() ->
                        new CustomApiException(ErrorCode.ORDER_NOT_FOUND)
                );

        Payment payment = paymentRepository
                .findByOrderId(order.getId())
                .orElseThrow(() ->
                        new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND)
                );

        validateRequest(payment, request);

        /*
         * 동일 transactionId의 재요청
         *
         * 1. 결제 + 지급 완료
         *    → 기존 성공 응답 반환
         *
         * 2. 결제 성공 + 지급 미완료
         *    → command를 null로 반환하여 fulfillment만 재시도
         */
        if (payment.isSuccess()
                && order.isSuccess()
                && Objects.equals(
                payment.getProviderTransactionId(),
                request.getTransactionIdentifier()
        )) {

            if (payment.isFulfilled()) {
                return new Confirmation(
                        payment.getId(),
                        null,
                        paymentMapper.toPaymentConfirmResponse(
                                order,
                                payment
                        )
                );
            }

            return new Confirmation(
                    payment.getId(),
                    null,
                    null
            );
        }

        // 다른 Payment가 동일 transactionId를 사용했는지 사전 검사
        paymentRepository
                .findByPaymentProviderAndProviderTransactionId(
                        payment.getPaymentProvider(),
                        request.getTransactionIdentifier()
                )
                .filter(existing ->
                        !existing.getId().equals(payment.getId())
                )
                .ifPresent(existing -> {
                    throw new CustomApiException(
                            ErrorCode.DUPLICATED_PAYMENT_TRANSACTION
                    );
                });

        PaymentVerificationCommand command =
                new PaymentVerificationCommand(
                        memberId,
                        order.getId(),
                        payment.getPaymentProvider(),
                        payment.getStoreProductId(),
                        request.getTransactionIdentifier(),
                        payment.getRequestedAmount()
                );

        return new Confirmation(
                payment.getId(),
                command,
                null
        );
    }

    private void validateRequest(
            Payment payment,
            PaymentConfirmRequest request
    ) {
        if (request.getProductIdentifier() == null
                || payment.getStoreProductId() == null
                || !Objects.equals(
                payment.getStoreProductId(),
                request.getProductIdentifier()
        )) {

            throw new CustomApiException(
                    ErrorCode.PRODUCT_IDENTIFIER_MISMATCH
            );
        }

        if (request.getTransactionIdentifier() == null
                || request.getTransactionIdentifier().isBlank()) {

            throw new CustomApiException(
                    ErrorCode.FAIL_CONFIRM_PAYMENT
            );
        }
    }

    private TransactionTemplate createReadTransaction() {
        TransactionTemplate read =
                new TransactionTemplate(transactionManager);

        read.setReadOnly(true);
        read.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW
        );

        return read;
    }

    @Override
    public String generate() {
        return "ORD-"
                + LocalDateTime.now().format(FORMATTER)
                + "-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    private record Confirmation(
            Long paymentId,
            PaymentVerificationCommand command,
            PaymentConfirmResponse response
    ) {
    }
}