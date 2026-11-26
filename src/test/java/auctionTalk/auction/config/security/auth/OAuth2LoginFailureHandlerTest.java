package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2LoginFailureHandlerTest {

    @Test
    @DisplayName("소셜 로그인 실패 응답에 내부 예외 메시지를 담지 않는다")
    void doesNotLeakExceptionMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token_response"), "token endpoint https://internal.example/oauth returned 500");

        new OAuth2LoginFailureHandler().onAuthenticationFailure(
                new MockHttpServletRequest("GET", "/login/oauth2/code/kakao"), response, exception);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains(ErrorCode.OAUTH2_LOGIN_FAILED.getCode())
                .doesNotContain("internal.example")
                .doesNotContain("invalid_token_response");
    }
}
