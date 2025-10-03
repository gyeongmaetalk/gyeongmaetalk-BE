package auctionTalk.auction.domain.member.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.dto.request.SignupRequest;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.global.common.BaseResponse;
import auctionTalk.auction.domain.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
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
}
