package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.dto.response.RevenueCatCustomerResponse;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.RevenueCatClient;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.RevenueCatPurchaseVerifier;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.RevenueCatVerifiedPurchase;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.domain.payment.service.PaymentFailureRecorder;
import auctionTalk.auction.domain.payment.service.PaymentFulfillmentService;
import auctionTalk.auction.domain.payment.service.PaymentSuccessProcessor;
import auctionTalk.auction.domain.product.entity.Product;
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
public class PaymentConfirmServiceImpl implements PaymentConfirmService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentFulfillmentService paymentFulfillmentService;
    private final RevenueCatClient revenueCatClient;
    private final RevenueCatPurchaseVerifier revenueCatPurchaseVerifier;
    private final PaymentFailureRecorder paymentFailureRecorder;
    private final PaymentSuccessProcessor paymentSuccessProcessor;


    @Override
    public PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest request) {
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));

        Product product = order.getProduct();

        if (payment.isSuccess() && order.isSuccess()) {
            return paymentMapper.toPaymentConfirmResponse(order, payment);
        }

        try {
            validateProductIdentifier(product, request.getProductIdentifier());
            validateDuplicatedTransaction(payment, request.getTransactionIdentifier());

            String revenueCatAppUserId = String.valueOf(memberId);

            RevenueCatCustomerResponse customerResponse =
                    revenueCatClient.getCustomer(revenueCatAppUserId);

            RevenueCatVerifiedPurchase verifiedPurchase =
                    revenueCatPurchaseVerifier.verifyNonSubscriptionPurchase(
                            customerResponse,
                            request.getProductIdentifier(),
                            request.getTransactionIdentifier()
                    );

            payment.markSuccessByRevenueCat(
                    verifiedPurchase.getTransactionIdentifier(),
                    verifiedPurchase.getProductIdentifier(),
                    verifiedPurchase.getStore(),
                    verifiedPurchase.getSandbox(),
                    verifiedPurchase.getPurchasedAt()
            );

            return paymentSuccessProcessor.processSuccess(
                    order.getId(),
                    payment.getId(),
                    verifiedPurchase
            );

        } catch (CustomApiException e) {
            paymentFailureRecorder.recordFailure(
                    order.getId(),
                    payment.getId(),
                    e.getErrorCode().getCode(),
                    e.getErrorCode().getMessage()
            );
            throw e;
        } catch (Exception e) {
            paymentFailureRecorder.recordFailure(
                    order.getId(),
                    payment.getId(),
                    "PAYMENT500",
                    e.getMessage()
            );
            throw new CustomApiException(ErrorCode.FAIL_CONFIRM_PAYMENT);
        }
    }

    @Override
    @Transactional
    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "ORD-" + timestamp + "-" + random;
    }


    private void validateProductIdentifier(Product product, String productIdentifier) {
        if (!product.getStoreProductId().equals(productIdentifier)) {
            throw new CustomApiException(ErrorCode.PRODUCT_IDENTIFIER_MISMATCH);
        }
    }

    private void validateDuplicatedTransaction(Payment currentPayment, String transactionIdentifier) {
        paymentRepository.findByPaymentProviderAndProviderTransactionId(
                        PaymentProvider.REVENUECAT,
                        transactionIdentifier
                )
                .filter(existingPayment -> !existingPayment.getId().equals(currentPayment.getId()))
                .ifPresent(existingPayment -> {
                    throw new CustomApiException(ErrorCode.DUPLICATED_PAYMENT_TRANSACTION);
                });
    }
}