package auctionTalk.auction.config.security;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class SecurityConstant {

    // 비로그인 허용 API
    public static final String[] PUBLIC_URLS = {
            "/v3/**", "/swagger-ui/**",

            // auth
            "/auth/exchange", "/auth/refresh",
            "/auth/signup",

            //s3
            "/s3/presigned/get",

            //qna
            "/qna/faq",

            //reviews
            "/reviews/*", "/reviews/list/**", "/reviews/list",

            //actuator
            "/actuator/prometheus",
            "/actuator/health",
            "/actuator/info",

            //admin
            "/admin/auth/login",

            //product
            "/products/**",
    };

    // 로그인 필요 API
    public static final String[] AUTHENTICATED_URLS = {
            // auth
            "/auth/delete", "/auth/info",
            "/auth/logout", "/auth/notification/setting",
            "/auth/sms", "/auth/sms/verify",

            //counsels
            "/counsels/**",

            //counselor
            "/counselor/**",

            //properties
            "/properties/**",

            //s3
            "/s3/presigned/**",

            //qna
            "/qna", "/qna/my",

            //reviews
            "/reviews", "/reviews/*/reports", "/reviews/my",

            //fcm
            "/fcm/token", "/fcm/notifications/setting",
            "/fcm/notifications", "/fcm/*/read",

            //order
            "/orders",

            //payment
            "/payments/confirm",

            //view-tickets
            "/view-tickets/**",

    };

    // 관리자 전용 API
    public static final String[] ADMIN_URLS = {
            "/admin/**",

            "/counselor", "/counselor/**",

            "/s3/presigned/**",

            "/qna/*/answer",
    };

    // 모든 경로를 포함한 배열 (필요할 경우 사용)
    public static final String[] ALL_URLS =
            Stream.of(PUBLIC_URLS, AUTHENTICATED_URLS, ADMIN_URLS)
                    .flatMap(Arrays::stream)
                    .toArray(String[]::new);
}