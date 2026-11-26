package auctionTalk.auction.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 인증을 쿠키(SameSite=None)로 하므로 CORS 허용 출처가 곧 CSRF 방어선이다.
 * 허용하지 않은 출처에서 온 요청은 Spring의 CorsFilter가 컨트롤러에 닿기 전에 403으로 거부한다
 * (폼 자동 제출처럼 프리플라이트가 없는 요청도 브라우저가 Origin 헤더를 붙이므로 함께 막힌다).
 * Origin 헤더가 없는 요청(앱·서버 간 호출)은 브라우저 요청이 아니므로 CSRF 대상이 아니다.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    // 애플 로그인은 appleid.apple.com에서 콜백 주소로 form_post한다
    private static final String APPLE_CALLBACK_PATH = "/login/oauth2/code/apple";
    private static final String APPLE_ORIGIN = "https://appleid.apple.com";

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        List<String> origins = properties.allowedOrigins();
        if (origins == null || origins.isEmpty()) {
            throw new IllegalStateException("app.cors.allowed-origins를 설정해야 합니다. 프론트엔드 출처(예: https://app.example.com)를 나열하세요.");
        }
        if (origins.stream().anyMatch(origin -> origin.trim().equals("*"))) {
            throw new IllegalStateException("app.cors.allowed-origins에 '*'를 쓸 수 없습니다. 쿠키 인증이라 모든 사이트가 사용자 권한으로 API를 호출할 수 있게 됩니다.");
        }

        CorsConfiguration api = new CorsConfiguration();
        api.setAllowedOriginPatterns(origins);
        api.setAllowCredentials(true);
        api.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        api.setAllowedHeaders(List.of("*"));
        api.setMaxAge(3600L);

        CorsConfiguration appleCallback = new CorsConfiguration();
        appleCallback.addAllowedOrigin(APPLE_ORIGIN);
        appleCallback.addAllowedMethod("POST");

        // 먼저 등록한 경로가 우선 매칭된다
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(APPLE_CALLBACK_PATH, appleCallback);
        source.registerCorsConfiguration("/**", api);
        return source;
    }
}
