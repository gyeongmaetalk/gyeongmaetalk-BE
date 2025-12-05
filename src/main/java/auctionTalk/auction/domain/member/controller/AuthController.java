package auctionTalk.auction.domain.member.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.dto.request.NotificationSettingRequest;
import auctionTalk.auction.domain.member.dto.request.SignupRequest;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.dto.response.MemberInfoResponse;
import auctionTalk.auction.domain.member.dto.response.NotificationSettingResponse;
import auctionTalk.auction.global.common.BaseResponse;
import auctionTalk.auction.domain.member.service.AuthService;
import auctionTalk.auction.utils.sms.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SmsService smsService;

    @Operation(summary = "코드 교환 API")
    @PostMapping("/exchange")
    @Parameters(value = {
            @Parameter(name = "code", description = "토큰으로 교환할 인증코드")
    })
    public BaseResponse<AuthTokenResponse> exchangeCode(
            @CookieValue(value = "code") String code
    ) {
        return BaseResponse.onSuccess(authService.exchangeCode(code));
    }

    @Operation(summary = "회원가입(회원 정보 입력) API")
    @PostMapping("/signup")
    public BaseResponse<MemberIdResponse> signup(
            @AuthenticationPrincipal PrincipalDetails member,
            @RequestBody SignupRequest request
    ) {
        return BaseResponse.onSuccess(authService.register(member.getMember(), request));
    }

    @Operation(summary = "토큰 재발급 API")
    @PostMapping("/refresh")
    public BaseResponse<AuthTokenResponse> refresh(
            @RequestHeader("RefreshToken") String refreshToken
    ) {
        return BaseResponse.onSuccess(authService.refresh(refreshToken));
    }

    @Operation(summary = "내 정보 조회 API")
    @GetMapping("/info")
    public BaseResponse<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal PrincipalDetails principal
    ){
        return BaseResponse.onSuccess(authService.getMemberInfo(principal.getMember()));
    }

    @Operation(summary = "휴대폰 인증번호 문자 전송 API")
    @PostMapping("/sms")
    @Parameters(value = {
            @Parameter(name = "phoneNumber", description = "인증할 전화번호")
    })
    public BaseResponse<String> sendSms(
            @RequestParam(name = "phoneNumber") String phoneNumber
    ){
        return BaseResponse.onSuccess(smsService.SendSms(phoneNumber));
    }

    @Operation(summary = "휴대폰 인증번호 확인 API")
    @PostMapping("/sms/verify")
    @Parameters(value = {
            @Parameter(name = "phoneNumber", description = "전화번호"),
            @Parameter(name = "code", description = "사용자가 입력한 인증번호")
    })
    public BaseResponse<Boolean> verifySms(
            @RequestParam String phoneNumber,
            @RequestParam String code
    ){
        return BaseResponse.onSuccess(smsService.verifyCode(phoneNumber, code));
    }

    @Operation(summary = "알림 설정 변경 API")
    @PatchMapping("/notification/setting")
    public BaseResponse<NotificationSettingResponse> updateNotificationSetting(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestBody NotificationSettingRequest request
    ){
        return BaseResponse.onSuccess(authService.updateNotificationSetting(principal.getMember(), request));
    }

    @Operation(summary = "회원탈퇴 API")
    @PostMapping("/delete")
    public BaseResponse<MemberIdResponse> softDeleteMember(
            @AuthenticationPrincipal PrincipalDetails principal
    ){
        return BaseResponse.onSuccess(authService.softDeleteMember(principal.getMember()));
    }

}
