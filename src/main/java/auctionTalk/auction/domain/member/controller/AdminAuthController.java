package auctionTalk.auction.domain.member.controller;

import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.service.AdminAuthService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 전용 인증 API", description = "어드민 전용 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "어드민 계정 생성 API")
    @PostMapping("/signup")
    @Parameters(value = {
            @Parameter(name = "username", description = "아이디"),
            @Parameter(name = "password", description = "패스워드")

    })
    public BaseResponse<MemberIdResponse> createAdminMember(
            @RequestParam String username,
            @RequestParam String password
    ) {
        return BaseResponse.onSuccess(adminAuthService.createAdmin(username, password));
    }

    @Operation(summary = "어드민 계정 로그인 API")
    @PostMapping("/login")
    @Parameters(value = {
            @Parameter(name = "username", description = "아이디"),
            @Parameter(name = "password", description = "패스워드")

    })
    public BaseResponse<MemberIdResponse> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response
    ) {

        AuthTokenResponse tokenResponse = adminAuthService.adminLogin(username, password);


        ResponseCookie accessTokenCookie = ResponseCookie.from("ACCESS_TOKEN", tokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(60 * 60)  // 1시간
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(60 * 60 * 24 * 14)  // 2주
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return BaseResponse.onSuccess(new MemberIdResponse(tokenResponse.getMemberId()));
    }
}
