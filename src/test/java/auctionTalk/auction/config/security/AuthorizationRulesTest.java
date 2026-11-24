package auctionTalk.auction.config.security;

import auctionTalk.auction.config.security.auth.AppleOidcUserService;
import auctionTalk.auction.config.security.auth.CustomOAuth2UserService;
import auctionTalk.auction.config.security.auth.HttpCookieOAuth2AuthorizationRequestRepository;
import auctionTalk.auction.config.security.auth.OAuth2LoginSuccessHandler;
import auctionTalk.auction.config.security.jwt.CustomAccessDeniedHandler;
import auctionTalk.auction.config.security.jwt.CustomAuthenticationEntryPoint;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * URL 인가 규칙만 검증한다. 컨트롤러는 띄우지 않으므로 보안 필터를 통과한 요청은 404가 된다.
 * ALLOW = 통과(401·403이 아님), 401 = 로그인 필요, 403 = 권한 없음
 */
@WebMvcTest(controllers = AuthorizationRulesTest.NoController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AuthorizationRulesTest {

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

    @ParameterizedTest(name = "{1} {2} as {0} -> {3}")
    @CsvSource({
            // 상담사 생성·수정은 관리자만 (기존: 로그인 규칙이 먼저 매칭돼 일반 회원도 통과)
            "USER,  POST,   /counselor,              403",
            "USER,  PATCH,  /counselor/1,            403",
            "ADMIN, POST,   /counselor,              ALLOW",
            "ADMIN, PATCH,  /counselor/1,            ALLOW",
            "USER,  GET,    /counselor/1,            ALLOW",
            "ANON,  GET,    /counselor/1,            401",

            // 리뷰: 조회만 공개 (기존: /reviews/* 가 메서드 구분 없이 공개라 수정·삭제·내 리뷰가 비로그인으로 컨트롤러까지 도달)
            "ANON,  PATCH,  /reviews/1,              401",
            "ANON,  DELETE, /reviews/1,              401",
            "ANON,  GET,    /reviews/my,             401",
            "USER,  GET,    /reviews/my,             ALLOW",
            "USER,  PATCH,  /reviews/1,              ALLOW",
            "ANON,  GET,    /reviews/1,              ALLOW",
            "ANON,  GET,    /reviews/list,           ALLOW",
            "ANON,  GET,    /reviews/list/3,         ALLOW",
            "ANON,  POST,   /reviews,                401",
            "ANON,  POST,   /reviews/1/reports,      401",

            // 회원가입 추가 정보 입력은 소셜 로그인 후 호출 (기존: 공개라 비로그인 시 NPE로 500)
            "ANON,  POST,   /auth/signup,            401",
            "USER,  POST,   /auth/signup,            ALLOW",

            // S3 업로드 URL은 사용자 웹의 리뷰 이미지 업로드에 쓰이므로 로그인 사용자 허용
            "USER,  GET,    /s3/presigned/put,       ALLOW",
            "ANON,  GET,    /s3/presigned/put,       401",
            "ANON,  GET,    /s3/presigned/get,       ALLOW",

            // 관리자 영역
            "ANON,  POST,   /admin/auth/login,       ALLOW",
            "USER,  POST,   /admin/auth/signup,      403",
            "USER,  GET,    /admin/qna/list,         403",
            "ADMIN, GET,    /admin/qna/list,         ALLOW",
            "USER,  POST,   /qna/1/answer,           403",
            "ADMIN, POST,   /qna/1/answer,           ALLOW",
            "USER,  POST,   /qna,                    ALLOW",

            // 그 밖의 공개 API
            "ANON,  GET,    /products,               ALLOW",
            "ANON,  GET,    /products/1,             ALLOW",
            "ANON,  POST,   /auth/refresh,           ALLOW",
            "ANON,  GET,    /qna/faq,                ALLOW",
            "ANON,  GET,    /app/config,             ALLOW",
            "ANON,  GET,    /actuator/health,        ALLOW",

            // 목록에 없는 경로는 로그인 필요
            "ANON,  GET,    /properties/1,           401",
            "ANON,  GET,    /anything-else,          401",
    })
    void urlAuthorization(String who, String method, String path, String expected) throws Exception {
        MockHttpServletRequestBuilder request = request(HttpMethod.valueOf(method), path);
        if (!who.equals("ANON")) {
            request.with(user("tester").roles(who));
        }

        int status = mockMvc.perform(request).andReturn().getResponse().getStatus();

        if (expected.equals("ALLOW")) {
            assertThat(status).as("%s %s as %s", method, path, who).isNotIn(401, 403);
        } else {
            assertThat(status).as("%s %s as %s", method, path, who).isEqualTo(Integer.parseInt(expected));
        }
    }
}
