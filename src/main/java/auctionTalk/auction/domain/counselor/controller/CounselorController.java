package auctionTalk.auction.domain.counselor.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.counselor.dto.request.CounselorCreateRequest;
import auctionTalk.auction.domain.counselor.dto.request.CounselorUpdateRequest;
import auctionTalk.auction.domain.counselor.dto.response.CounselorIdResponse;
import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.counselor.service.CounselorService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "상담사 API", description = "상담사 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/counselor")
public class CounselorController {

    private final CounselorService counselorService;

    @Operation(summary = "상담사 생성 API")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<CounselorIdResponse> createCounselor(
            @Parameter(description = "상담사 이미지 파일들(없을 시 사용 x)") @RequestPart(value = "counselorImages", required = false) List<MultipartFile> counselorImages,
            @Parameter(description = "상담사 생성 요청 json")  @Valid @RequestPart("request") CounselorCreateRequest request

    ){
        return BaseResponse.onSuccess(counselorService.createCounselor(request,counselorImages));
    }

    @Operation(summary = "상담사 수정 API")
    @PatchMapping(value = "/{counselorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<CounselorIdResponse> updateCounselor(
            @Parameter(description = "수정할 상담사 id") @PathVariable Long counselorId,
            @Parameter(description = "상담사 이미지 파일들(없을 시 사용 x)") @RequestPart(value = "counselorImages", required = false) List<MultipartFile> counselorImages,
            @Parameter(description = "상담사 수정 요청 json")  @Valid @RequestPart("request") CounselorUpdateRequest request
    ){
        return BaseResponse.onSuccess(counselorService.updateCounselor(counselorId,request,counselorImages));
    }

    @Operation(summary = "상담사 정보 조회 API")
    @GetMapping("/{counselorId}")
    public BaseResponse<CounselorResponse> inquiryCounselor(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("counselorId") Long counselorId
    ){
        return BaseResponse.onSuccess(counselorService.inquiryCounselor(counselorId, principal.getMember()));
    }
}
