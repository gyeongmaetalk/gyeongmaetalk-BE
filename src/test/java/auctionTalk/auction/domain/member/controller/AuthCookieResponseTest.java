package auctionTalk.auction.domain.member.controller;

import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.service.AdminAuthService;
import auctionTalk.auction.domain.member.service.AuthService;
import auctionTalk.auction.global.exception.CustomRestControllerAdvice;
import auctionTalk.auction.utils.sms.SmsService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class AuthCookieResponseTest {

    private static final String ACCESS = "access-token-value";
    private static final String REFRESH = "refresh-token-value";
    private static final long REFRESH_COOKIE_SECONDS = Duration.ofDays(15).toSeconds(); // 리프레시 토큰 만료와 같아야 함

    private final AuthService authService = mock(AuthService.class);
    private final AdminAuthService adminAuthService = mock(AdminAuthService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthTokenResponse tokens = AuthTokenResponse.builder().memberId(1L).accessToken(ACCESS).refreshToken(REFRESH).build();
        given(authService.refresh(anyString())).willReturn(tokens);
        given(adminAuthService.adminLogin(anyString(), anyString())).willReturn(tokens);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AuthController(authService, mock(SmsService.class)),
                        new AdminAuthController(adminAuthService))
                .setControllerAdvice(new CustomRestControllerAdvice())
                .build();
    }

    @Test
    @DisplayName("토큰 재발급 응답 본문에는 토큰이 없다 (HttpOnly 쿠키로만 전달)")
    void refreshDoesNotExposeTokensInBody() throws Exception {
        MockHttpServletResponse res = mockMvc.perform(post("/auth/refresh").cookie(new Cookie("REFRESH_TOKEN", "old")))
                .andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getContentAsString()).doesNotContain(ACCESS).doesNotContain(REFRESH);
        assertThat(res.getCookie("ACCESS_TOKEN").getValue()).isEqualTo(ACCESS);
    }

    @Test
    @DisplayName("관리자 로그인은 아이디·비밀번호를 요청 본문(JSON)으로 받는다")
    void adminLoginTakesJsonBody() throws Exception {
        MockHttpServletResponse res = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"pw\"}"))
                .andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getContentAsString()).doesNotContain(ACCESS).doesNotContain(REFRESH);
    }

    @Test
    @DisplayName("관리자 비밀번호를 URL 쿼리로 보내면 거부한다 (접근 로그에 비밀번호가 남음)")
    void adminLoginRejectsCredentialsInUrl() throws Exception {
        int status = mockMvc.perform(post("/admin/auth/login").queryParam("username", "admin").queryParam("password", "pw"))
                .andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(400);
    }

    @Test
    @DisplayName("관리자 리프레시 쿠키 수명이 리프레시 토큰 만료(15일)와 같다 (기존: 14일)")
    void adminRefreshCookieMatchesTokenLifetime() throws Exception {
        MockHttpServletResponse res = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"pw\"}")
                        .queryParam("username", "admin").queryParam("password", "pw")) // 기존 코드에서도 호출되도록
                .andReturn().getResponse();

        assertThat(res.getHeaders("Set-Cookie"))
                .anyMatch(h -> h.startsWith("REFRESH_TOKEN=") && h.contains("Max-Age=" + REFRESH_COOKIE_SECONDS));
    }
}
