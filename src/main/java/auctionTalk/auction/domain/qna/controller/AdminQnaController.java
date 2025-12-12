package auctionTalk.auction.domain.qna.controller;

import auctionTalk.auction.domain.qna.dto.response.AdminQnaInquiryResponse;
import auctionTalk.auction.domain.qna.dto.response.AdminQnaPagingResponse;
import auctionTalk.auction.domain.qna.service.AdminQnaService;
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

@Tag(name = "어드민 전용 문의 API", description = "어드민 문의 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/qna")
public class AdminQnaController {

    private final AdminQnaService adminQnaService;

    @Operation(summary = "어드민 용 문의 조회 API")
    @GetMapping("/list")
    @Parameters(value = {
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<AdminQnaPagingResponse<AdminQnaInquiryResponse>> inquiryAdminQna(
            @RequestParam int size,
            @RequestParam int page
    ){
        return BaseResponse.onSuccess(adminQnaService.inquiryAdminQna(size, page));
    }
}
