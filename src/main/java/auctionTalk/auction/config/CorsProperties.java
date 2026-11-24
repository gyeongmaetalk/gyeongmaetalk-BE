package auctionTalk.auction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 쿠키 인증 요청을 허용할 프론트엔드 출처 목록.
 * 예) app.cors.allowed-origins: https://gyeongmaetalk.example, https://admin.gyeongmaetalk.example, http://localhost:5173
 * 패턴(https://*.example.com)도 쓸 수 있지만, 누구나 하위 도메인을 만들 수 있는 공용 도메인(*.vercel.app 등)은 넣으면 안 된다.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
