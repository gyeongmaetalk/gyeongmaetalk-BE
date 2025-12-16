package auctionTalk.auction.domain.payment.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.payment.dto.request.AdminPaymentSearchRequest;
import auctionTalk.auction.domain.payment.dto.response.AdminInquiryPayment;
import auctionTalk.auction.domain.payment.dto.response.AdminPaymentPagingResponse;
import auctionTalk.auction.domain.payment.entity.PaymentType;
import auctionTalk.auction.domain.payment.service.PaymentQueryService;
import auctionTalk.auction.domain.payment.service.PaymentService;
import auctionTalk.auction.domain.property.dto.response.PropertyIdResponse;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "어드민 용 결제 관련 API", description = "어드민 용 결제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/payments")
public class AdminPaymentController {

    private final PaymentQueryService paymentQueryService;

    @Operation(summary = "어드민용 결제 페이징 조회")
    @GetMapping("/list")
    @Parameters(value = {
            @Parameter(name = "paymentType", description = "필터링 결제 타입"),
            @Parameter(name = "startDate", description = "필터링 시작 날짜"),
            @Parameter(name = "endDate", description = "필터링 끝 날짜"),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<AdminPaymentPagingResponse<AdminInquiryPayment>> InquiryPayments(
            @RequestParam PaymentType paymentType,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam int page,
            @RequestParam int size
            ){
        return BaseResponse.onSuccess(paymentQueryService.InquiryPayments(paymentType, startDate, endDate, page, size));
    }
}
