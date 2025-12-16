package auctionTalk.auction.domain.counsel.controller;

import auctionTalk.auction.domain.counsel.dto.response.AdminCounselPagingResponse;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselResponse;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.service.AdminCounselService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "어드민 용 상담 API", description = "어드민 용 상담 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/counsels")
public class AdminCounselController {

    private final AdminCounselService adminCounselService;

    @Operation(summary = "상담 목록 조회 API")
    @GetMapping("/list")
    @Parameters(value = {
            @Parameter(name = "statuses", description = "필터링 할 상담 상태 목록"),
            @Parameter(name = "startDate", description = "필터링 시작 날짜"),
            @Parameter(name = "endDate", description = "필터링 끝 날짜"),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<AdminCounselPagingResponse<AdminCounselResponse>> inquiryCounselsByCounselStatus(
            @RequestParam List<CounselStatus> statuses,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam int page,
            @RequestParam int size
    ){
        return BaseResponse.onSuccess(adminCounselService.inquiryCounselsByCounselStatus(statuses, startDate, endDate, page, size));
    }
}
