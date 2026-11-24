package auctionTalk.auction.config.security.jwt;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.member.repository.TokenRepository;
import auctionTalk.auction.global.exception.CustomRestControllerAdvice;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.exception.JwtAuthenticationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JwtTokenTypeTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("0123456789abcdef".repeat(4).getBytes());
    private static final long MEMBER_ID = 7L;

    private final TokenRepository tokenRepository = mock(TokenRepository.class);
    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, tokenRepository);
    private final JwtAuthorizationFilter filter = new JwtAuthorizationFilter(provider, memberRepository);

    private JwtToken tokens;

    @BeforeEach
    void setUp() {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(MEMBER_ID);
        given(member.getRole()).willReturn(Role.USER);
        given(memberRepository.getMember(anyLong())).willReturn(member);

        tokens = provider.generateTokens(MEMBER_ID, Role.USER.name());
        given(tokenRepository.getRefreshToken(MEMBER_ID)).willReturn(tokens.getRefreshToken());
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ---------- 인증 필터 ----------

    @Test
    @DisplayName("액세스 토큰은 인증된다")
    void accessTokenAuthenticates() throws Exception {
        Result result = filter(tokens.getAccessToken());

        assertThat(result.passed).isTrue();
        assertThat(result.authenticated).isTrue();
    }

    @Test
    @DisplayName("리프레시 토큰으로는 API를 호출할 수 없다 (로그아웃 후 15일간 쓰이던 문제)")
    void refreshTokenIsRejectedAsAccessToken() throws Exception {
        Result result = filter(tokens.getRefreshToken());

        assertThat(result.passed).isFalse();
        assertThat(result.status).isEqualTo(401);
        assertThat(result.body).contains(ErrorCode.INVALID_JWT_TOKEN.getCode());
    }

    @Test
    @DisplayName("용도 정보가 없는 기존 토큰은 거부된다")
    void legacyTokenWithoutTypeIsRejected() throws Exception {
        String legacy = Jwts.builder()
                .setSubject(String.valueOf(MEMBER_ID))
                .claim("memberId", MEMBER_ID)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)), SignatureAlgorithm.HS512)
                .compact();

        Result result = filter(legacy);

        assertThat(result.passed).isFalse();
        assertThat(result.status).isEqualTo(401);
    }

    @Test
    @DisplayName("서명이 변조된 토큰은 필터에서 401로 끝난다 (예외가 필터 밖으로 새지 않음)")
    void tamperedSignatureIsRejectedInFilter() throws Exception {
        String token = tokens.getAccessToken();
        String tampered = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");

        Result result = filter(tampered);

        assertThat(result.passed).isFalse();
        assertThat(result.status).isEqualTo(401);
        assertThat(result.body).contains(ErrorCode.INVALID_JWT_TOKEN.getCode());
    }

    @Test
    @DisplayName("형식이 깨진 토큰은 401")
    void malformedTokenIsRejected() throws Exception {
        Result result = filter("not-a-jwt");

        assertThat(result.passed).isFalse();
        assertThat(result.status).isEqualTo(401);
    }

    // ---------- 재발급 ----------

    @Test
    @DisplayName("리프레시 토큰은 재발급에 쓸 수 있다")
    void refreshTokenRefreshes() {
        assertThat(provider.validateAndExtractRefreshToken(tokens.getRefreshToken()).getMemberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("액세스 토큰으로는 재발급할 수 없다")
    void accessTokenCannotRefresh() {
        assertThatThrownBy(() -> provider.validateAndExtractRefreshToken(tokens.getAccessToken()))
                .hasMessage(ErrorCode.INVALID_JWT_TOKEN.getMessage());
    }

    @Test
    @DisplayName("형식이 깨진 리프레시 토큰은 JWT 인증 예외로 바뀐다 (라이브러리 예외가 그대로 나가 500이 되던 문제)")
    void malformedRefreshTokenIsMapped() {
        assertThatThrownBy(() -> provider.validateAndExtractRefreshToken("not-a-jwt"))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessage(ErrorCode.INVALID_JWT_TOKEN.getMessage());
    }

    @Test
    @DisplayName("컨트롤러에서 난 JWT 인증 예외는 500이 아니라 해당 에러 코드로 응답한다")
    void jwtExceptionFromControllerIsUnauthorized() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new CustomRestControllerAdvice())
                .build();

        mockMvc.perform(post("/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.EXPIRED_JWT_TOKEN.getCode()));
    }

    @RestController
    static class ThrowingController {
        @PostMapping("/refresh")
        void refresh() {
            throw new JwtAuthenticationException(ErrorCode.EXPIRED_JWT_TOKEN);
        }
    }

    // ---------- helpers ----------

    private record Result(boolean passed, boolean authenticated, int status, String body) {
    }

    private Result filter(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/info");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        boolean authenticated = SecurityContextHolder.getContext().getAuthentication() != null;
        return new Result(chain.getRequest() != null, authenticated, response.getStatus(), response.getContentAsString());
    }
}
