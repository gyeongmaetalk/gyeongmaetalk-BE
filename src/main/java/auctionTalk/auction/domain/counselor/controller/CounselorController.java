package auctionTalk.auction.domain.counselor.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.counselor.service.CounselorService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "상담사 API", description = "상담사 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/counselor")
public class CounselorController {

    private final CounselorService counselorService;

    @Operation(summary = "상담사 정보 조회 API")
    @GetMapping("/{counselorId}")
    public BaseResponse<CounselorResponse> inquiryCounselor(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("counselorId") Long counselorId
    ){
        return BaseResponse.onSuccess(counselorService.inquiryCounselor(counselorId, principal.getMember()));
    }
}
