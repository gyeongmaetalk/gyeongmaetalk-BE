package auctionTalk.auction.domain.payment.controller;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;
import auctionTalk.auction.domain.payment.service.verify.PaymentConfirmService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "Payment", description = "결제 API")
public class PaymentController {

    private final PaymentConfirmService paymentConfirmService;

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "스토어 결제 결과를 서버에서 검증하고 주문을 완료 처리.")
    public BaseResponse<PaymentConfirmResponse> confirmPayment(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return BaseResponse.onSuccess(paymentConfirmService.confirm(member.getId(), request));
    }
}