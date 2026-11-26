package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.utils.CookieUtils;
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

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

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

            if ("apple".equals(registrationId) && canUpdateAppleName(member)) {
                String appleName = extractAppleName(request);

                if (StringUtils.hasText(appleName)) {
                    member.updateName(appleName);
                    memberRepository.save(member); // ✅ SuccessHandler는 트랜잭션 아님
                    log.info("애플 첫 로그인 이름 저장 완료. memberId={}", memberId);
                }
            }
        }

        // generateTokens가 리프레시 토큰을 Redis에 저장한다. 쿠키 속성·수명은 CookieUtils 한 곳에서 관리한다.
        JwtToken jwtToken = jwtTokenProvider.generateTokens(memberId, member.getRole().name());
        CookieUtils.addJwtCookies(response, jwtToken);

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

        if (!StringUtils.hasText(userJson)) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(userJson);
            JsonNode nameNode = root.path("name");

            String firstName = nameNode.path("firstName").asText(null);
            String lastName = nameNode.path("lastName").asText(null);

            StringBuilder fullName = new StringBuilder();

            if (StringUtils.hasText(lastName)) {
                fullName.append(lastName);
            }

            if (StringUtils.hasText(firstName)) {
                fullName.append(firstName);
            }

            return StringUtils.hasText(fullName.toString()) ? fullName.toString() : null;

        } catch (Exception e) {
            log.warn("애플 사용자 이름 파싱 실패: {}", e.getClass().getSimpleName()); // 원문에는 이름·이메일이 있어 남기지 않는다
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

    private boolean canUpdateAppleName(Member member) {
        return !StringUtils.hasText(member.getName())
                || "사용자".equals(member.getName())
                || "unknown".equalsIgnoreCase(member.getName())
                || "null".equalsIgnoreCase(member.getName());
    }
}