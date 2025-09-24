package auctionTalk.auction.config.security.jwt;

import auctionTalk.auction.global.common.BaseResponse;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.exception.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED; // 기본값

        if (authException instanceof JwtAuthenticationException jwtEx) {
            errorCode = jwtEx.getErrorCode();
        }

        BaseResponse<Object> errorResponse = BaseResponse.onFailure(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
