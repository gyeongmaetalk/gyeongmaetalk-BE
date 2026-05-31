package auctionTalk.auction.domain.order.controller;

import auctionTalk.auction.domain.order.dto.request.OrderCreateRequest;
import auctionTalk.auction.domain.order.dto.response.OrderCreateResponse;
import auctionTalk.auction.domain.order.service.OrderService;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.product.entity.ProductType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() throws Exception {
        // given
        OrderCreateRequest request = OrderCreateRequest.builder()
                .productId(1L)
                .idempotencyKey("test-idempotency-key")
                .paymentProvider(PaymentProvider.APPLE)
                .counselorId(10L)
                .build();

        OrderCreateResponse response = OrderCreateResponse.builder()
                .orderId(1L)
                .orderNumber("ORD-20260415180000-AB12CD34")
                .amount(300000L)
                .paymentProvider(PaymentProvider.APPLE)
                .productId(1L)
                .productName("경매 대행 신청")
                .productType(ProductType.SINGLE)
                .storeProductId("auction_application")
                .build();

        given(orderService.createOrder(any(), any(OrderCreateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123"))
                .andExpect(jsonPath("$.paymentNumber").value("PAY-123"))
                .andExpect(jsonPath("$.storeProductId").value("auction_application"))
                .andExpect(jsonPath("$.orderStatus").value("READY"))
                .andExpect(jsonPath("$.paymentStatus").value("READY"));
    }
}