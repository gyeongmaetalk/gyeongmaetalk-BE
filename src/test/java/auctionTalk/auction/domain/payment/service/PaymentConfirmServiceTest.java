package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.entity.*;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.entity.*;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.domain.payment.service.verify.*;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.global.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class PaymentConfirmServiceTest {
    PaymentRepository payments = mock(PaymentRepository.class);
    OrderRepository orders = mock(OrderRepository.class);
    PaymentVerificationService verifier = mock(PaymentVerificationService.class);
    PaymentFulfillmentService fulfillment = mock(PaymentFulfillmentService.class);
    EntityManager em = mock(EntityManager.class);
    PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    PaymentConfirmServiceImpl service;
    Order order;
    Payment payment;
    PaymentVerificationResult result;

    @BeforeEach
    void setUp() {
        when(transactions.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());

        when(verifier.supportProvider())
                .thenReturn(PaymentProvider.REVENUECAT);

        PaymentMapper mapper = new PaymentMapper();

        PaymentSuccessProcessor successProcessor =
                new PaymentSuccessProcessor(
                        payments,
                        em
                );

        PaymentFulfillmentProcessor fulfillmentProcessor =
                new PaymentFulfillmentProcessor(
                        payments,
                        fulfillment,
                        mapper,
                        em
                );

        service = new PaymentConfirmServiceImpl(
                payments,
                orders,
                mapper,
                new PaymentVerificationServiceResolver(List.of(verifier)),
                successProcessor,
                fulfillmentProcessor,
                transactions
        );

        order = Order.builder()
                .id(1L)
                .member(
                        Member.builder()
                                .id(2L)
                                .build()
                )
                .product(
                        Product.builder()
                                .id(3L)
                                .name("ticket")
                                .build()
                )
                .orderStatus(OrderStatus.READY)
                .build();

        payment = Payment.builder()
                .id(4L)
                .order(order)
                .paymentProvider(PaymentProvider.REVENUECAT)
                .paymentStatus(PaymentStatus.PENDING)
                .storeProductId("ticket")
                .build();

        result = PaymentVerificationResult.builder()
                .provider(PaymentProvider.REVENUECAT)
                .providerTransactionId("tx")
                .storeProductId("ticket")
                .build();

        when(orders.findByIdAndMemberId(1L, 2L))
                .thenReturn(Optional.of(order));

        when(payments.findByOrderId(1L))
                .thenReturn(Optional.of(payment));

        when(payments.findByIdForUpdate(4L))
                .thenReturn(Optional.of(payment));

        when(verifier.verify(any()))
                .thenReturn(result);
    }

    PaymentConfirmRequest request(String product, String tx) throws Exception {
        return new ObjectMapper().readValue("{\"orderId\":1,\"productIdentifier\":\"" + product
                + "\",\"transactionIdentifier\":\"" + tx + "\"}", PaymentConfirmRequest.class);
    }

    @Test void verifiedPaymentCompletesOrderAndFulfills() throws Exception {
        var response = service.confirm(2L, request("ticket", "tx"));
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(fulfillment).fulfill(order, payment);
        verify(verifier).verify(new PaymentVerificationCommand(2L, 1L, PaymentProvider.REVENUECAT, "ticket", "tx", null));
    }

    @Test void unknownProductDoesNotVerifyOrFulfill() throws Exception {
        assertThatThrownBy(() -> service.confirm(2L, request("missing", "tx")))
                .isInstanceOfSatisfying(CustomApiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_IDENTIFIER_MISMATCH));
        verify(verifier, never()).verify(any());
        assertUnchanged();
    }

    @Test void verificationFailureLeavesOrderPending() throws Exception {
        when(verifier.verify(any())).thenThrow(new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED));
        assertThatThrownBy(() -> service.confirm(2L, request("ticket", "tx"))).isInstanceOf(CustomApiException.class);
        assertUnchanged();
    }

    @Test void anotherOrdersTransactionIsRejected() throws Exception {
        when(payments.findByPaymentProviderAndProviderTransactionId(PaymentProvider.REVENUECAT, "tx"))
                .thenReturn(Optional.of(Payment.builder().id(99L).build()));
        assertThatThrownBy(() -> service.confirm(2L, request("ticket", "tx")))
                .isInstanceOfSatisfying(CustomApiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_PAYMENT_TRANSACTION));
        verify(verifier, never()).verify(any());
        assertUnchanged();
    }

    @Test void repeatedConfirmDoesNotVerifyOrFulfillTwice() throws Exception {
        service.confirm(2L, request("ticket", "tx"));
        service.confirm(2L, request("ticket", "tx"));
        verify(verifier, times(1)).verify(any());
        verify(fulfillment, times(1)).fulfill(order, payment);
    }

    @Test void normalizedAliasIsAlsoIdempotent() throws Exception {
        service.confirm(2L, request("ticket", "alias"));
        service.confirm(2L, request("ticket", "alias"));
        verify(verifier, times(2)).verify(any());
        verify(fulfillment, times(1)).fulfill(order, payment);
    }

    @Test void canonicalTransactionCollisionIsRejectedAfterVerification() throws Exception {
        when(payments.findByPaymentProviderAndProviderTransactionId(PaymentProvider.REVENUECAT, "tx"))
                .thenReturn(Optional.of(Payment.builder().id(99L).build()));
        assertThatThrownBy(() -> service.confirm(2L, request("ticket", "alias"))).isInstanceOf(CustomApiException.class);
        verify(verifier).verify(any());
        assertUnchanged();
    }

    @Test void differentTransactionCannotReplaceSuccessfulPayment() throws Exception {
        service.confirm(2L, request("ticket", "tx"));
        when(verifier.verify(any())).thenReturn(PaymentVerificationResult.builder().provider(PaymentProvider.REVENUECAT)
                .storeProductId("ticket").providerTransactionId("other").build());
        assertThatThrownBy(() -> service.confirm(2L, request("ticket", "other"))).isInstanceOf(CustomApiException.class);
        assertThat(payment.getProviderTransactionId()).isEqualTo("tx");
        verify(fulfillment, times(1)).fulfill(order, payment);
    }

    @Test void missingCanonicalTransactionCannotSucceed() throws Exception {
        when(verifier.verify(any())).thenReturn(PaymentVerificationResult.builder()
                .provider(PaymentProvider.REVENUECAT).storeProductId("ticket").build());
        assertThatThrownBy(() -> service.confirm(2L, request("ticket", "tx"))).isInstanceOf(CustomApiException.class);
        assertUnchanged();
    }

    @Test void wrongVerifiedProductCannotSucceed() throws Exception {
        when(verifier.verify(any())).thenReturn(PaymentVerificationResult.builder()
                .provider(PaymentProvider.REVENUECAT).storeProductId("wrong").providerTransactionId("tx").build());
        assertThatThrownBy(() -> service.confirm(2L, request("ticket", "tx"))).isInstanceOf(CustomApiException.class);
        assertUnchanged();
    }

    @Test void resolverRejectsDuplicateRegistration() {
        assertThatThrownBy(() -> new PaymentVerificationServiceResolver(List.of(verifier, verifier)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test void resolverRejectsUnsupportedProvider() {
        assertThatThrownBy(() -> new PaymentVerificationServiceResolver(List.of()).resolve(PaymentProvider.REVENUECAT))
                .isInstanceOf(CustomApiException.class);
    }

    void assertUnchanged() {
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.READY);
        verifyNoInteractions(fulfillment);
    }
}
