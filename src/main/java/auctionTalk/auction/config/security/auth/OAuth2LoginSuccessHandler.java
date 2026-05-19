package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.member.repository.TokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 60L * 60 * 1000;           // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 15L * 24 * 60 * 60 * 1000; // 15일

    @Value("${app.redirect-url.dev}")
    private String devRedirectUrl;

    @Value("${app.redirect-url.preview}")
    private String previewRedirectUrl;

    @Value("${app.redirect-url.prod}")
    private String prodRedirectUrl;

    @Value("${app.oauth.default-client-env:DEV}")
    private String defaultClientEnv;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Member member = principalDetails.getMember();
        Long memberId = member.getId();

        //애플 첫 로그인 시 이름 받아오기
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            if ("apple".equals(registrationId) && !StringUtils.hasText(member.getName())) {
                String appleName = extractAppleName(request);

                if (StringUtils.hasText(appleName)) {
                    member.updateName(appleName);
                    memberRepository.save(member); // ✅ SuccessHandler는 트랜잭션 아님
                    log.info("🍎 Apple name 저장 완료: {}", appleName);
                }
            }
        }

        JwtToken jwtToken = jwtTokenProvider.generateTokens(memberId, member.getRole().name());
        String accessToken = jwtToken.getAccessToken();
        String refreshToken = jwtToken.getRefreshToken();

        tokenRepository.saveRefreshToken(memberId, refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(ACCESS_TOKEN_EXPIRE_TIME))
                .sameSite("None")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME))
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String baseRedirectUrl = resolveRedirectUrl(request);

        String redirectUrl = baseRedirectUrl
                + "?registered=" + member.isRegistered()
                + "&name=" + URLEncoder.encode(
                Optional.ofNullable(member.getName()).orElse(""),
                StandardCharsets.UTF_8
        );

        deleteClientEnvCookie(response);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractAppleName(HttpServletRequest request) {
        String userJson = request.getParameter("user");

        log.info("[APPLE_USER_PARAM] user={}", userJson);

        if (!StringUtils.hasText(userJson)) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(userJson);
            JsonNode nameNode = root.path("name");

            String firstName = nameNode.path("firstName").asText(null);
            String lastName = nameNode.path("lastName").asText(null);

            log.info("[APPLE_NAME_PARSED] firstName={}, lastName={}", firstName, lastName);

            StringBuilder fullName = new StringBuilder();

            if (StringUtils.hasText(lastName)) {
                fullName.append(lastName);
            }

            if (StringUtils.hasText(firstName)) {
                fullName.append(firstName);
            }

            return StringUtils.hasText(fullName.toString()) ? fullName.toString() : null;

        } catch (Exception e) {
            log.warn("[APPLE_NAME_PARSE_FAILED] userJson={}", userJson, e);
            return null;
        }
    }

    private String resolveRedirectUrl(HttpServletRequest request) {
        ClientEnv clientEnv = resolveClientEnv(request);

        return switch (clientEnv) {
            case DEV -> devRedirectUrl;
            case PREVIEW -> previewRedirectUrl;
            case PROD -> prodRedirectUrl;
        };
    }

    private ClientEnv resolveClientEnv(HttpServletRequest request) {
        ClientEnv defaultEnv = ClientEnv.fromOrDefault(defaultClientEnv, ClientEnv.DEV);

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return defaultEnv;
        }

        for (Cookie cookie : cookies) {
            if (OAuthClientEnvCookieFilter.COOKIE_NAME.equals(cookie.getName())) {
                return ClientEnv.fromOrDefault(cookie.getValue(), defaultEnv);
            }
        }

        return defaultEnv;
    }

    private void deleteClientEnvCookie(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from(OAuthClientEnvCookieFilter.COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}