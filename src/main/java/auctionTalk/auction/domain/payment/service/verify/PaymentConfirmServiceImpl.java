package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.domain.payment.service.PaymentFulfillmentService;
import auctionTalk.auction.domain.product.entity.Product;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentConfirmServiceImpl implements PaymentConfirmService {

    private static final String FAILURE_CODE_VERIFY_FAILED = "PAYMENT_VERIFY_FAILED";
    private static final String FAILURE_CODE_DUPLICATED_TRANSACTION = "DUPLICATED_PROVIDER_TRANSACTION";
    private static final String FAILURE_CODE_PRODUCT_MISMATCH = "STORE_PRODUCT_MISMATCH";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentVerificationServiceResolver verificationServiceResolver;
    private final PaymentMapper paymentMapper;
    private final PaymentFulfillmentService paymentFulfillmentService;


    @Override
    @Transactional
    public PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest request) {
        Order order = orderRepository.findByIdAndMemberId(request.getOrderId(), memberId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        Product product = order.getProduct();

        if (payment.isSuccess() && order.isSuccess()) {
            return paymentMapper.toPaymentConfirmResponse(order, payment, product);
        }

        payment.markVerifying(request.getProvider(), request.getStoreProductId(), extractPurchaseToken(request));

        PaymentVerificationService verificationService =
                verificationServiceResolver.resolve(request.getProvider());

        PaymentVerificationResult result;
        try {
            result = verificationService.verify(request);
        } catch (RuntimeException e) {
            payment.markFailed(
                    request.getProvider(),
                    extractPurchaseToken(request),
                    request.getStoreProductId(),
                    request.getProviderVerificationData(),
                    FAILURE_CODE_VERIFY_FAILED,
                    e.getMessage(),
                    LocalDateTime.now()
            );
            order.markFail();
            throw e;
        }

        if (!Objects.equals(product.getStoreProductId(), result.getStoreProductId())) {
            payment.markFailed(
                    request.getProvider(),
                    extractPurchaseToken(request),
                    result.getStoreProductId(),
                    result.getRawVerificationData(),
                    FAILURE_CODE_PRODUCT_MISMATCH,
                    "스토어 검증 결과 상품 ID가 주문 상품과 다릅니다.",
                    LocalDateTime.now()
            );
            order.markFail();
            throw new IllegalStateException("스토어 검증 결과 상품 ID가 주문 상품과 다릅니다.");
        }

        boolean duplicated = paymentRepository.existsByPaymentProviderAndProviderTransactionId(
                request.getProvider(),
                result.getProviderTransactionId()
        );

        if (duplicated && !Objects.equals(payment.getProviderTransactionId(), result.getProviderTransactionId())) {
            payment.markFailed(
                    request.getProvider(),
                    extractPurchaseToken(request),
                    result.getStoreProductId(),
                    result.getRawVerificationData(),
                    FAILURE_CODE_DUPLICATED_TRANSACTION,
                    "이미 처리된 스토어 거래입니다.",
                    LocalDateTime.now()
            );
            order.markFail();
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }

        payment.markApproved(
                request.getProvider(),
                result.getProviderTransactionId(),
                extractPurchaseToken(request),
                result.getStoreProductId(),
                result.getRawVerificationData(),
                order.getAmount(),
                result.getApprovedAt()
        );

        order.markComplete();
        paymentFulfillmentService.fulfill(order);

        return paymentMapper.toPaymentConfirmResponse(order, payment, product);
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
    private String extractPurchaseToken(PaymentConfirmRequest request) {
        return request.getProvider() == PaymentProvider.GOOGLE
                ? request.getProviderVerificationData()
                : null;
    }
}