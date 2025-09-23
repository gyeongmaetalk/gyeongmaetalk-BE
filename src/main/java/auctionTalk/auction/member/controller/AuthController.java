package auctionTalk.auction.member.controller;

import auctionTalk.api.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.api.domain.member.entity.LoginType;
import auctionTalk.api.domain.member.service.AuthService;
import auctionTalk.api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인 API")
    @PostMapping("/login")
    public BaseResponse<AuthTokenResponse> login(
            @RequestParam(name = "accessToken") String accessToken,
            @RequestParam(name = "provider") LoginType loginType
    ) {
        return BaseResponse.onSuccess(authService.login(accessToken, loginType));
    }

    @Operation(summary = "토큰 재발급 API")
    @PostMapping("/refresh")
    public BaseResponse<AuthTokenResponse> refresh(
            @RequestHeader("RefreshToken") String refreshToken
    ) {
        return BaseResponse.onSuccess(authService.refresh(refreshToken));
    }
}
