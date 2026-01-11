package auctionTalk.auction.domain.property.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.request.PropertyCreateRequest;
import auctionTalk.auction.domain.property.dto.request.PropertyUpdateRequest;
import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyIdResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;
import auctionTalk.auction.domain.property.service.AdminPropertyService;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionIdResponse;
import auctionTalk.auction.domain.subscription.service.AdminSubscriptionService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 용 추천 매물 API", description = "어드민 용 추천 매물 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/properties")
public class AdminPropertyController {

    private final AdminPropertyService adminPropertyService;
    private final AdminSubscriptionService adminSubscriptionService;

    @Operation(summary = "어드민 추천 매물 생성 API")
    @PostMapping
    public BaseResponse<PropertyIdResponse> createProperty(
            @RequestBody PropertyCreateRequest request
    ) {
        return BaseResponse.onSuccess(adminPropertyService.createProperty(request));
    }
    
    @Operation(summary = "어드민 추천 매물 수정 API")
    @PatchMapping("/{propertyId}")
    @Parameters(value = {
            @Parameter(name = "propertyId", description = "수정할 추천 매물 Id"),
    })
    public BaseResponse<PropertyIdResponse> updateProperty(
            @PathVariable(name = "propertyId") Long propertyId,
            @RequestBody PropertyUpdateRequest request
    ) {
        return BaseResponse.onSuccess(adminPropertyService.updateProperty(propertyId, request));
    }

    @Operation(summary = "어드민 추천 매물 삭제 API")
    @DeleteMapping("/{propertyId}")
    @Parameters(value = {
            @Parameter(name = "propertyId", description = "삭제할 추천 매물 Id"),
    })
    public BaseResponse<PropertyIdResponse> deleteProperty(
            @PathVariable(name = "propertyId") Long propertyId
    ) {
        return BaseResponse.onSuccess(adminPropertyService.deleteProperty(propertyId));
    }

    @Operation(summary = "어드민 추천 매물 상세조회 API")
    @GetMapping("/{propertyId}/detail")
    @Parameters(value = {
            @Parameter(name = "propertyId", description = "조회할 추천 매물 Id"),
    })
    public BaseResponse<PropertyDetailResponse> inquiryPropertyDetail(
            @PathVariable(name = "propertyId") Long propertyId
    ) {
        return BaseResponse.onSuccess(adminPropertyService.getPropertyDetail(propertyId));
    }

    @Operation(summary = "어드민 추천 매물 목록 조회 API")
    @GetMapping("/list")
    @Parameters(value = {
            @Parameter(name = "memberId", description = "조회할 멤버 Id"),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<PropertyPagingResponse<PropertySummaryResponse>> inquiryPropertiesByMemberId(
            @RequestParam(name = "memberId") Long memberId,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ) {
        return BaseResponse.onSuccess(adminPropertyService.inquiryPropertiesByMember(memberId, page, size));
    }

    @Operation(summary = "어드민 추천 매물 구독 결제 상태 변경(승인 or 취소) API")
    @PatchMapping("/subscription/{subscriptionId}/status")
    public BaseResponse<SubscriptionIdResponse> confirmSubscriptionPayment(
            @RequestParam Long memberId,
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam PaymentStatus status
    ){
        return BaseResponse.onSuccess(adminSubscriptionService.updateSubscriptionStatus(memberId, subscriptionId, status));
    }

    @Operation(summary = "어드민 추천 매물 구매 결제 상태 변경(승인 or 취소) API")
    @PatchMapping("/{propertyId}/status")
    public BaseResponse<PaymentResultResponse> confirmPropertyPayment(
            @PathVariable("propertyId") Long propertyId,
            @RequestParam PaymentStatus status
    ) {
        return BaseResponse.onSuccess(adminPropertyService.updatePropertyPaymentStatus(propertyId, status));
    }


}
