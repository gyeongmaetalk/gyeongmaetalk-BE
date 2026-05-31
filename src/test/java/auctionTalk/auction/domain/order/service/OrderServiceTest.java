package auctionTalk.auction.domain.order.service;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.dto.request.OrderCreateRequest;
import auctionTalk.auction.domain.order.dto.response.OrderCreateResponse;
import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.order.entity.OrderStatus;
import auctionTalk.auction.domain.order.mapper.OrderMapper;
import auctionTalk.auction.domain.order.repository.OrderRepository;
import auctionTalk.auction.domain.payment.entity.Payment;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.mapper.PaymentMapper;
import auctionTalk.auction.domain.payment.repository.PaymentRepository;
import auctionTalk.auction.domain.payment.service.verify.PaymentConfirmService;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.entity.ProductType;
import auctionTalk.auction.domain.product.repository.ProductRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentConfirmService paymentConfirmService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("기존 주문이 없으면 주문과 결제를 새로 생성한다")
    void createOrder_newOrder_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .productId(1L)
                .idempotencyKey("idem-key")
                .counselorId(10L)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("경매 대행 신청")
                .price(300000L)
                .productType(ProductType.SINGLE)
                .storeProductId("auction_application")
                .build();

        Order order = Order.builder()
                .id(1L)
                .member(member)
                .product(product)
                .orderNumber("ORD-20260415180000-AB12CD34")
                .idempotencyKey("idem-key")
                .orderStatus(OrderStatus.FAILED)
                .amount(product.getPrice())
                .counselorId(10L)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .order(order)
                .paymentNumber("PAY-123")
                .storeProductId("auction_application")
                .build();

        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId(1L)
                .orderNumber("ORD-20260415180000-AB12CD34")
                .amount(300000L)
                .paymentProvider(PaymentProvider.REVENUECAT)
                .productId(1L)
                .productName("경매 대행 신청")
                .productType(ProductType.SINGLE)
                .storeProductId("auction_application")
                .build();

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(orderRepository.findByMemberIdAndIdempotencyKey(1L, "idem-key"))
                .willReturn(Optional.empty());

        given(orderMapper.toOrder(
                eq(member),
                eq(product),
                anyString(),
                eq("idem-key"),
                eq(10L)
        )).willReturn(order);

        given(paymentConfirmService.generate()).willReturn("PAY-123");

        given(paymentMapper.toPayment(order, "PAY-123", "auction_application"))
                .willReturn(payment);

        given(orderMapper.toCreateResponse(order, payment))
                .willReturn(response);

        // when
        OrderCreateResponse result = orderService.createOrder(member, request);

        // then
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-20260415180000-AB12CD34");
        assertThat(result.getAmount()).isEqualTo(300000L);
        assertThat(result.getPaymentProvider()).isEqualTo(PaymentProvider.REVENUECAT);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("경매 대행 신청");
        assertThat(result.getProductType()).isEqualTo(ProductType.SINGLE);
        assertThat(result.getStoreProductId()).isEqualTo("auction_application");

        verify(orderRepository).save(order);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("같은 멤버와 같은 멱등성 키의 기존 주문이 있으면 기존 주문을 반환한다")
    void createOrder_existingOrder_returnExisting() {
        // given
        Member member = Member.builder()
                .id(1L)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .productId(1L)
                .idempotencyKey("idem-key")
                .counselorId(10L)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("경매 대행 신청")
                .price(300000L)
                .productType(ProductType.SINGLE)
                .storeProductId("auction_application")
                .build();

        Order existingOrder = Order.builder()
                .id(1L)
                .member(member)
                .product(product)
                .orderNumber("ORD-20260415180000-AB12CD34")
                .idempotencyKey("idem-key")
                .counselorId(10L)
                .build();

        Payment existingPayment = Payment.builder()
                .id(1L)
                .order(existingOrder)
                .paymentNumber("PAY-123")
                .storeProductId("auction_application")
                .build();

        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId(1L)
                .orderNumber("ORD-20260415180000-AB12CD34")
                .amount(300000L)
                .productId(1L)
                .productName("경매 대행 신청")
                .productType(ProductType.SINGLE)
                .storeProductId("auction_application")
                .build();

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(orderRepository.findByMemberIdAndIdempotencyKey(1L, "idem-key"))
                .willReturn(Optional.of(existingOrder));
        given(paymentRepository.findByOrderId(existingOrder.getId()))
                .willReturn(Optional.of(existingPayment));
        given(orderMapper.toCreateResponse(existingOrder, existingPayment))
                .willReturn(response);

        // when
        OrderCreateResponse result = orderService.createOrder(member, request);

        // then
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-20260415180000-AB12CD34");
        assertThat(result.getAmount()).isEqualTo(300000L);
        assertThat(result.getPaymentProvider()).isEqualTo(PaymentProvider.REVENUECAT);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("경매 대행 신청");
        assertThat(result.getProductType()).isEqualTo(ProductType.SINGLE);
        assertThat(result.getStoreProductId()).isEqualTo("auction_application");

        verify(orderRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentConfirmService, never()).generate();
    }

    @Test
    @DisplayName("상품이 없으면 예외가 발생한다")
    void createOrder_productNotFound() {
        // given
        Member member = Member.builder()
                .id(1L)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .productId(999L)
                .idempotencyKey("idem-key")
                .counselorId(10L)
                .build();

        given(productRepository.findById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(member, request))
                .isInstanceOf(CustomApiException.class);

        verify(orderRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentConfirmService, never()).generate();
    }
}