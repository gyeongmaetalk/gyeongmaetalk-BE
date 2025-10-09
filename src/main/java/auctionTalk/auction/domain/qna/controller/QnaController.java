package auctionTalk.auction.domain.qna.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.qna.dto.request.QnaCreateRequest;
import auctionTalk.auction.domain.qna.dto.response.FaqResponse;
import auctionTalk.auction.domain.qna.dto.response.QnaIdResponse;
import auctionTalk.auction.domain.qna.dto.response.QnaResponse;
import auctionTalk.auction.domain.qna.service.QnaService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "문의 API", description = "문의 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/qna")
public class QnaController {

    private final QnaService qnaService;

    @Operation(summary = "1:1 문의 생성 API")
    @PostMapping
    public BaseResponse<QnaIdResponse> createQna(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody QnaCreateRequest request
            ){
        return BaseResponse.onSuccess(qnaService.createQna(request, principal.getMember()));
    }

    @Operation(summary = "문의 답변 API")
    @PostMapping("/{qnaId}/answer")
    @Parameters(value = {
            @Parameter(name = "content", description = "질문 답변 내용")
    })
    public BaseResponse<QnaIdResponse> answerQna(
            @PathVariable("qnaId") Long qnaId,
            @RequestParam(name = "content") String content
    ){
        return BaseResponse.onSuccess(qnaService.answerQna(qnaId, content));
    }

    @Operation(summary = "내 문의 목록 조회 API")
    @GetMapping("/my")
    public BaseResponse<List<QnaResponse>> inquiryQnaByMember(
            @AuthenticationPrincipal PrincipalDetails principal
    ){
        return BaseResponse.onSuccess(qnaService.inquiryQnaByMember(principal.getMember()));
    }

    @Operation(summary = "자주 묻는 질문 목록 조회 API")
    @GetMapping("/faq")
    public BaseResponse<List<FaqResponse>> inquiryFaq(){
        return BaseResponse.onSuccess(qnaService.inquiryFaqList());
    }


}
