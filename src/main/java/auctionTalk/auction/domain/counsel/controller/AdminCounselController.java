package auctionTalk.auction.domain.counsel.controller;

import auctionTalk.auction.domain.counsel.dto.request.AdminCounselSearchRequest;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselPagingResponse;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselResponse;
import auctionTalk.auction.domain.counsel.service.AdminCounselService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 용 상담 API", description = "어드민 용 상담 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/counsels")
public class AdminCounselController {

    private final AdminCounselService adminCounselService;

    @Operation(summary = "상담 목록 조회 API")
    @GetMapping("/list")
    public BaseResponse<AdminCounselPagingResponse<AdminCounselResponse>> inquiryCounselsByCounselStatus(
            @RequestBody AdminCounselSearchRequest request
    ){
        return BaseResponse.onSuccess(adminCounselService.inquiryCounselsByCounselStatus(request));
    }
}
