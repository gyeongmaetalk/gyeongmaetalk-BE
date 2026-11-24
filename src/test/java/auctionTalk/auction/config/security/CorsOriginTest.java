package auctionTalk.auction.config.security;

import auctionTalk.auction.config.CorsConfig;
import auctionTalk.auction.config.security.auth.AppleOidcUserService;
import auctionTalk.auction.config.security.auth.CustomOAuth2UserService;
import auctionTalk.auction.config.security.auth.HttpCookieOAuth2AuthorizationRequestRepository;
import auctionTalk.auction.config.security.auth.OAuth2LoginSuccessHandler;
import auctionTalk.auction.config.security.jwt.CustomAccessDeniedHandler;
import auctionTalk.auction.config.security.jwt.CustomAuthenticationEntryPoint;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * CORS 허용 출처와, 그로 인한 쿠키 인증 CSRF 차단을 검증한다.
 * 컨트롤러는 띄우지 않으므로 보안 필터를 통과한 요청은 404가 된다.
 */
@WebMvcTest(controllers = CorsOriginTest.NoController.class,
        properties = "app.cors.allowed-origins=https://app.example.com,https://admin.example.com")
@Import({SecurityConfig.class, CorsConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class CorsOriginTest {

    private static final String ALLOWED = "https://app.example.com";
    private static final String EVIL = "https://evil.example";

    @RestController
    static class NoController {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private MemberRepository memberRepository;
    @MockBean private CustomOAuth2UserService customOAuth2UserService;
    @MockBean private AppleOidcUserService appleOidcUserService;
    @MockBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    @MockBean private ClientRegistrationRepository clientRegistrationRepository;
    @MockBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("허용한 출처는 쿠키를 포함한 응답을 읽을 수 있다")
    void allowedOriginGetsCorsHeaders() throws Exception {
        MockHttpServletResponse res = mockMvc.perform(get("/products").header(HttpHeaders.ORIGIN, ALLOWED))
                .andReturn().getResponse();

        assertThat(res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo(ALLOWED);
        assertThat(res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)).isEqualTo("true");
    }

    @Test
    @DisplayName("다른 출처의 조회는 거부되고 응답을 읽을 수 없다 (기존: 모든 출처를 그대로 허용)")
    void unknownOriginCannotReadResponse() throws Exception {
        MockHttpServletResponse res = mockMvc.perform(get("/products").header(HttpHeaders.ORIGIN, EVIL))
                .andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(403);
        assertThat(res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    @DisplayName("다른 출처의 프리플라이트는 거부된다")
    void unknownOriginPreflightIsRejected() throws Exception {
        MockHttpServletResponse res = mockMvc.perform(options("/auth/delete")
                        .header(HttpHeaders.ORIGIN, EVIL)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andReturn().getResponse();

        assertThat(res.getStatus()).isEqualTo(403);
        assertThat(res.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
    }

    @Test
    @DisplayName("다른 출처의 폼 자동 제출로 회원 탈퇴를 호출할 수 없다 (CSRF)")
    void crossSiteFormPostIsRejected() throws Exception {
        int status = mockMvc.perform(post("/auth/delete")
                        .with(user("victim").roles("USER"))
                        .header(HttpHeaders.ORIGIN, EVIL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(403);
    }

    @Test
    @DisplayName("Origin이 null인 요청(샌드박스 iframe 등)도 거부된다")
    void nullOriginIsRejected() throws Exception {
        int status = mockMvc.perform(post("/auth/delete")
                        .with(user("victim").roles("USER"))
                        .header(HttpHeaders.ORIGIN, "null"))
                .andReturn().getResponse().getStatus();

        assertThat(status).isEqualTo(403);
    }

    @Test
    @DisplayName("허용한 출처의 요청과 Origin이 없는 요청(앱·서버 호출)은 통과한다")
    void allowedOrNoOriginPasses() throws Exception {
        int fromApp = mockMvc.perform(post("/auth/delete").with(user("u").roles("USER")).header(HttpHeaders.ORIGIN, ALLOWED))
                .andReturn().getResponse().getStatus();
        int noOrigin = mockMvc.perform(post("/auth/delete").with(user("u").roles("USER")))
                .andReturn().getResponse().getStatus();

        assertThat(fromApp).isNotIn(401, 403);
        assertThat(noOrigin).isNotIn(401, 403);
    }

    @Test
    @DisplayName("애플 로그인 콜백(appleid.apple.com에서 form_post)은 CORS에 막히지 않는다")
    void appleFormPostCallbackIsNotBlockedByCors() throws Exception {
        MockHttpServletResponse res = mockMvc.perform(post("/login/oauth2/code/apple")
                        .header(HttpHeaders.ORIGIN, "https://appleid.apple.com")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("code", "c").param("state", "s"))
                .andReturn().getResponse();

        assertThat(res.getContentAsString()).doesNotContain("Invalid CORS request");
        assertThat(res.getStatus()).isNotEqualTo(403);
    }
}
