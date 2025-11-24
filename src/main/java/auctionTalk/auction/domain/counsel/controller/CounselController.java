package auctionTalk.auction.domain.counsel.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormUpdateRequest;
import auctionTalk.auction.domain.counsel.dto.response.ApplyCounselResponse;
import auctionTalk.auction.domain.counsel.dto.response.CounselCombinedResponse;
import auctionTalk.auction.domain.counsel.dto.response.MatchCounselorResponse;
import auctionTalk.auction.domain.counsel.service.CounselService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "상담 API", description = "상담 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/counsels")
public class CounselController {

    private final CounselService counselService;

    @Operation(summary = "상담사 매칭 API")
    @PostMapping("/matches")
    public BaseResponse<MatchCounselorResponse> matchCounselor(
            @AuthenticationPrincipal PrincipalDetails member,
            @Parameter(description = "상담사 매칭 요청 json")  @Valid @RequestBody CounselFormCreateRequest request
            ){
        return BaseResponse.onSuccess(counselService.matchCounselor(request, member.getMember()));
    }

    @Operation(summary = "상담 신청 폼 수정 API")
    @PatchMapping("/{counselFormId}")
    public BaseResponse<MatchCounselorResponse> updateCounselFormCounselor(
            @AuthenticationPrincipal PrincipalDetails member,
            @Parameter(description = "상담 신청 폼 수정 요청 json")  @Valid @RequestBody CounselFormUpdateRequest request,
            @PathVariable("counselFormId")  Long counselFormId
    ){
        return BaseResponse.onSuccess(counselService.updateCounselForm(counselFormId, request, member.getMember()));
    }

    @Operation(summary = "상담 신청 API")
    @PostMapping("/{counselorId}")
    @Parameters(value = {
            @Parameter(name = "counselorId", description = "상담사 id"),
            @Parameter(name = "counselFormId", description = "상담 신청 폼 id"),
            @Parameter(name = "date", description = "희망 시간"),
    })
    public BaseResponse<ApplyCounselResponse> applyCounsel(
            @AuthenticationPrincipal PrincipalDetails member,
            @PathVariable("counselorId") Long counselorId,
            @RequestParam(name = "counselFormId") Long counselFormId,
            @RequestParam(name = "date") LocalDateTime dateTime
    ){
        return BaseResponse.onSuccess(counselService.applyCounsel(counselFormId, member.getMember(), counselorId, dateTime.toLocalDate(), dateTime.toLocalTime()));
    }

    @Operation(summary = "상담 신청 수정 API")
    @PatchMapping("/{counselId}/update")
    @Parameters(value = {
            @Parameter(name = "counselId", description = "상담 id"),
            @Parameter(name = "counselorId", description = "상담사 id"),
            @Parameter(name = "counselFormId", description = "상담 신청 폼 id"),
            @Parameter(name = "date", description = "희망 시간"),
    })
    public BaseResponse<ApplyCounselResponse> updateCounsel(
            @AuthenticationPrincipal PrincipalDetails member,
            @PathVariable("counselId") Long counselId,
            @RequestParam(name = "counselorId") Long counselorId,
            @RequestParam(name = "counselFormId") Long counselFormId,
            @RequestParam(name = "date") LocalDateTime dateTime
    ){
        return BaseResponse.onSuccess(counselService.updateApplyCounsel(counselId, counselFormId, member.getMember(), counselorId, dateTime.toLocalDate(), dateTime.toLocalTime()));
    }

    @Operation(summary = "상담 가능 시간 조회 API")
    @GetMapping("/{counselorId}/times")
    @Parameters(value = {
            @Parameter(name = "counselorId", description = "상담사 id"),
            @Parameter(name = "date", description = "조회할 날짜"),
    })
    public BaseResponse<List<LocalTime>> inquiryPossibleTime(
            @PathVariable("counselorId") Long counselorId,
            @RequestParam(name = "date") LocalDate date
    ){
        return BaseResponse.onSuccess(counselService.inquiryPossibleTime(counselorId, date));
    }

    @Operation(summary = "상담 정보 조회 API")
    @GetMapping("/info")
    public BaseResponse<CounselCombinedResponse> getCounselInfo(
            @AuthenticationPrincipal PrincipalDetails principal
    ){
        return BaseResponse.onSuccess(counselService.getCounselInfo(principal.getMember()));
    }

}
