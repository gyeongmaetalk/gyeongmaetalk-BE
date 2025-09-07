package auctionTalk.auction.global.security.filter;


import auctionTalk.auction.global.common.CommonResponse;
import auctionTalk.auction.global.common.exception.base.GlobalErrorCode;
import auctionTalk.auction.global.security.provider.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
// 들어오는 요청 처리
public class JwtAuthFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    private final String[] whiteList;


    /* 요청이 들어올 때마다 실행.
     * 토큰 확인, 토큰 유효성 검사, 토큰에 포함된 정보를 기반으로 인증 수행 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("jwt 인증 시작, access token 인증 헤더 정보 : {}, refresh 토큰 인증 헤더 정보 : {}", request.getHeader("Authorization"), request.getHeader("Refresh"));
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = request.getHeader(headerName);
//            log.info("인증 헤더 모두 null이기 때문에 헤더 정보 다 출력 => {} : {}", headerName, headerValue);
//        }

        if (request.getHeader("Authorization") == null && request.getHeader("Refresh") == null) {
//            Enumeration<String> headerNames = request.getHeaderNames();

//            while (headerNames.hasMoreElements()) {
//                String headerName = headerNames.nextElement();
//                String headerValue = request.getHeader(headerName);
//                log.info("인증 헤더 모두 null이기 때문에 헤더 정보 다 출력 => {} : {}", headerName, headerValue);
//            }
        }

        // HTTP 요청에서 Authorization헤더를 찾아 토큰 반환
        String accessToken = tokenProvider.resolveToken(request, "Authorization");

        if (accessToken == null) {
            accessToken = tokenProvider.resolveToken(request, "Refresh");

            if (StringUtils.hasText(accessToken) && tokenProvider.validateRefreshToken(accessToken)){
                Authentication authentication = tokenProvider.getRefreshAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
                SecurityContextHolder.getContext().setAuthentication(null);
            }
            // 다음 단계 실행 -> 다른 필터 및 컨트롤러 실행
            filterChain.doFilter(request,response);

        }else {


            // 토큰이 있다면 진행
            if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {

                Authentication authentication = tokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보를 SecurityContext에 설정

            } else {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
            // 다음 단계 실행 -> 다른 필터 및 컨트롤러 실행
            filterChain.doFilter(request, response);
        }
    }


    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        String refreshToken = request.getHeader("Refresh-Token");
        if (StringUtils.hasText(refreshToken)) {
            return refreshToken;
        }
        return null;
    }


    // JWT 인증과 관련된 예외 처리
    public void jwtExceptionHandler(HttpServletResponse response, GlobalErrorCode errorCode) {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");

        try {
            // AuthErrorCode로부터 code와 message 추출
            String code = errorCode.getCode();
            String message = errorCode.getMessage();
            String json = new ObjectMapper().writeValueAsString(CommonResponse.onFailure(code, message, null)); // ApiResponse 객체를 JSON으로 변환
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean pass = Arrays.stream(whiteList).anyMatch(path::contains);
        log.info("shouldNotFilter? path={}, result={}", path, pass);
        return pass;
    }
}

