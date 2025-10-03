package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;


import java.io.IOException;

@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        // ✅ 기존 AuthServiceImpl 로직 그대로 재사용
        AuthTokenResponse tokenResponse = authService.login(principalDetails.getMember());

        String redirectUrl = "https://frontend.com/oauth/success?accessToken="
                + tokenResponse.getAccessToken()
                + "&refreshToken=" + tokenResponse.getRefreshToken();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}