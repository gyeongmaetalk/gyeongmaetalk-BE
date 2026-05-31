package auctionTalk.auction.domain.order.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.order.dto.request.OrderCreateRequest;
import auctionTalk.auction.domain.order.dto.response.OrderCreateResponse;
import auctionTalk.auction.domain.order.service.OrderService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "상품 주문 API", description = "상품 주문 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성(결제 준비)", description = "상품 결제를 위한 주문과 결제 대기 데이터를 생성합니다.")
    public BaseResponse<OrderCreateResponse> createOrder(
            @AuthenticationPrincipal PrincipalDetails member,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return BaseResponse.onSuccess(orderService.createOrder(member.getMember(), request));
    }
}
