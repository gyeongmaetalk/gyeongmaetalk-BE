package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.CodeRepository;
import auctionTalk.auction.domain.member.repository.TokenRepository;
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

    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 60L * 60 * 1000;           // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 15L * 24 * 60 * 60 * 1000; // 15일

    @Value("${app.redirect-url}")
    private String baseRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Member member = principalDetails.getMember();
        Long memberId = member.getId();

        JwtToken jwtToken = jwtTokenProvider.generateTokens(memberId, member.getRole().name());
        String accessToken = jwtToken.getAccessToken();
        String refreshToken = jwtToken.getRefreshToken();

        tokenRepository.saveRefreshToken(memberId, refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(ACCESS_TOKEN_EXPIRE_TIME))
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(REFRESH_TOKEN_EXPIRE_TIME))
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String redirectUrl = baseRedirectUrl
                + "?registered=" + member.isRegistered();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}