package auctionTalk.auction.domain.member.controller;

import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.service.AdminAuthService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import auctionTalk.auction.domain.member.dto.request.AdminCredentialRequest;
import auctionTalk.auction.utils.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "어드민 전용 인증 API", description = "어드민 전용 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "어드민 계정 생성 API")
    @PostMapping("/signup")
    public BaseResponse<MemberIdResponse> createAdminMember(
            @RequestBody AdminCredentialRequest request
    ) {
        return BaseResponse.onSuccess(adminAuthService.createAdmin(request.username(), request.password()));
    }

    // 비밀번호가 URL(접근 로그)에 남지 않도록 요청 본문(JSON)으로만 받는다
    @Operation(summary = "어드민 계정 로그인 API")
    @PostMapping("/login")
    public BaseResponse<MemberIdResponse> login(
            @RequestBody AdminCredentialRequest request,
            HttpServletResponse response
    ) {
        AuthTokenResponse tokenResponse = adminAuthService.adminLogin(request.username(), request.password());

        CookieUtils.addJwtCookies(response, tokenResponse.toJwtToken());

        return BaseResponse.onSuccess(new MemberIdResponse(tokenResponse.getMemberId()));
    }
}
