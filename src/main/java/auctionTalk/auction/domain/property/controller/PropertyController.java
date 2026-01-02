package auctionTalk.auction.domain.property.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.property.dto.response.*;
import auctionTalk.auction.domain.property.service.PropertyService;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.service.SubscriptionService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "추천 매물 API", description = "추천 매물 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final SubscriptionService subscriptionService;


    @Operation(summary = "추천 매물 구매 신청 API")
    @PostMapping("/{propertyId}/purchases")
    public BaseResponse<PropertyIdResponse> purchaseProperty(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("propertyId") Long propertyId
    ){
        return BaseResponse.onSuccess(propertyService.purchaseProperty(principal.getMember(),  propertyId));
    }
    
    @Operation(summary = "추천 매물 목록 조회 API")
    @GetMapping("/list")
    @Parameters(value = {
            @Parameter(name = "isPurchased", description = "구매 여부에 따른 조회(null은 전체 조회)"),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<PropertyPagingResponse<PropertySummaryResponse>> inquiryProperties(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(name = "isPurchased",required = false) Boolean isPurchased,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ) {
        return BaseResponse.onSuccess(propertyService.inquiryProperties(principal, isPurchased, page, size));
    }

    @Operation(summary = "추천 매물 상세 조회 API(결제 시 조회 가능)")
    @GetMapping("/{propertyId}")
    public BaseResponse<PropertyDetailResponse> inquiryPropertyDetail(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("propertyId") Long propertyId
    ) {
        return BaseResponse.onSuccess(propertyService.inquiryPropertyDetail(principal.getMember(), propertyId));
    }

    @Operation(summary = "추천 매물 구독 신청(결제 준비) API")
    @PostMapping("/{counselorId}/subscribe")
    public BaseResponse<SubscriptionIdResponse> prepareSubscriptionPayment(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("counselorId") Long counselorId
    ){
        return BaseResponse.onSuccess(subscriptionService.prepareSubscriptionPayment(principal.getMember(), counselorId));
    }

    @Operation(summary = "추천 매물 구독 결제 완료(결제 승인) API")
    @PostMapping("/subscribe/{subscriptionId}/confirm")
    public BaseResponse<PaymentResultResponse> confirmSubscriptionPayment(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestBody PaymentConfirmRequest request
    ){
        return BaseResponse.onSuccess(subscriptionService.confirmSubscriptionPayment(principal.getMember(), subscriptionId, request));
    }

    @Operation(summary = "추천 매물 구매 결제 준비 API")
    @PostMapping("/{propertyId}/prepare")
    public BaseResponse<PropertyPreparePaymentResponse> confirmSubscriptionPayment(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("propertyId") Long propertyId
    ){
        return BaseResponse.onSuccess(propertyService.preparePropertyPayment(principal.getMember(), propertyId));
    }

    @Operation(summary = "추천 매물 구매 결제 승인 API")
    @PostMapping("/{propertyId}/confirm")
    public BaseResponse<PaymentResultResponse> confirmPropertyPayment(
            @PathVariable("propertyId") Long propertyId,
            @RequestBody PaymentConfirmRequest request
    ) {
        return BaseResponse.onSuccess(propertyService.confirmPropertyPayment(propertyId, request));
    }

//    @Operation(summary = "추천 매물 계약 완료 신청 API")
//    @PostMapping("/subscribe/{subscriptionId}/complete")
//    public BaseResponse<SubscriptionIdResponse> completeSubscription(
//            @PathVariable("subscriptionId") Long subscriptionId
//    ){
//        return BaseResponse.onSuccess(subscriptionService.completeSubscription(subscriptionId));
//    }
}
