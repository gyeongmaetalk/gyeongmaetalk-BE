package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.global.common.BaseResponse;
import auctionTalk.auction.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 소셜 로그인 실패 응답. 원인은 서버 로그에만 남기고, 클라이언트에는 고정된 에러 코드만 보낸다.
 * (기존: 예외 메시지를 응답에 그대로 담고 printStackTrace로 표준 에러에 출력)
 */
@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("OAuth2 로그인 실패. uri={}, reason={}", request.getRequestURI(), exception.getMessage());

        ErrorCode errorCode = ErrorCode.OAUTH2_LOGIN_FAILED;
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null)));
    }
}
