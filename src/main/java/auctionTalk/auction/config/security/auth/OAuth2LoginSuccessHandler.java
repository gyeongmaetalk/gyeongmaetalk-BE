package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.CodeRepository;
import auctionTalk.auction.domain.member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CodeRepository codeRepository;

    @Value("${app.redirect-url}")
    private String baseRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Member member = principalDetails.getMember();

        String code = UUID.randomUUID().toString();
        codeRepository.save(code, member.getId());

        ResponseCookie serverCodeCookie = ResponseCookie.from("server_code", code)
                .httpOnly(true)              // JS에서 접근 불가
                .secure(true)                // HTTPS 에서만 전송
                .path("/")                   // 전체 경로에 대해 전송
                .maxAge(Duration.ofMinutes(3))
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, serverCodeCookie.toString());

        String redirectUrl = baseRedirectUrl
                + "?registered=" + member.isRegistered();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}