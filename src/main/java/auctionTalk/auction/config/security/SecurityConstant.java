package auctionTalk.auction.config.security;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class SecurityConstant {

    // 비로그인 허용 API
    public static final String[] PUBLIC_URLS = {
    };

    // 로그인 필요 API
    public static final String[] AUTHENTICATED_URLS = {
    };

    // 관리자 전용 API
    public static final String[] ADMIN_URLS = {
            "/admin/**"
    };

    // 모든 경로를 포함한 배열 (필요할 경우 사용)
    public static final String[] ALL_URLS =
            Stream.of(PUBLIC_URLS, AUTHENTICATED_URLS, ADMIN_URLS)
                    .flatMap(Arrays::stream)
                    .toArray(String[]::new);
}