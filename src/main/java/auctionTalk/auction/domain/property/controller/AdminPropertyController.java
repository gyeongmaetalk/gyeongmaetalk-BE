package auctionTalk.auction.domain.property.controller;

import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;
import auctionTalk.auction.domain.property.service.AdminPropertyService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 용 추천 매물 API", description = "어드민 용 추천 매물 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/properties")
public class AdminPropertyController {

    private final AdminPropertyService adminPropertyService;

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
}
