package auctionTalk.auction.domain.subscription.controller;

import auctionTalk.auction.domain.counsel.dto.request.AdminCounselSearchRequest;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselPagingResponse;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselResponse;
import auctionTalk.auction.domain.counsel.service.AdminCounselService;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPagingResponse;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionResponse;
import auctionTalk.auction.domain.subscription.service.AdminSubscriptionService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 용 구독 API", description = "어드민 용 구독 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subscriptions")
public class AdminSubscribeController {

    private final AdminSubscriptionService adminSubscriptionService;

    @Operation(summary = "구독 정보 목록 조회 API")
    @GetMapping("/list")
    public BaseResponse<SubscriptionPagingResponse<SubscriptionResponse>> inquirySubscriptions(
            int page, int size
    ){
        return BaseResponse.onSuccess(adminSubscriptionService.inquirySubscriptions(page, size));
    }
}
