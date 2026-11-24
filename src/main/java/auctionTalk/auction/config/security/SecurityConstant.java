package auctionTalk.auction.config.security;

/**
 * URL 인가 규칙에 쓰는 경로 목록.
 * SecurityConfig는 규칙을 위에서부터 보고 처음 매칭되는 하나만 적용하므로, 적용 순서는 SecurityConfig의 주석을 따른다.
 * 여기 없는 경로는 모두 로그인이 필요하다.
 */
public final class SecurityConstant {

    private SecurityConstant() {
    }

    // 메서드와 관계없이 비로그인 허용
    public static final String[] PUBLIC_URLS = {
            "/v3/**", "/swagger-ui/**",

            // auth
            "/auth/exchange", "/auth/refresh",

            // s3
            "/s3/presigned/get",

            // qna
            "/qna/faq",

            // actuator
            "/actuator/prometheus",
            "/actuator/health",
            "/actuator/info",

            // admin (관리자 영역 안의 유일한 공개 경로)
            "/admin/auth/login",

            // app
            "/app/config",
    };

    // 관리자 전용 (메서드 무관)
    public static final String[] ADMIN_URLS = {
            "/admin/**",
            "/qna/*/answer",
    };

    // 관리자 전용 POST: 상담사 생성
    public static final String[] ADMIN_POST_URLS = {
            "/counselor",
    };

    // 관리자 전용 PATCH: 상담사 수정
    public static final String[] ADMIN_PATCH_URLS = {
            "/counselor/*",
    };

    // 아래 PUBLIC_GET_URLS 패턴에 걸리지만 로그인이 필요한 조회
    public static final String[] AUTHENTICATED_GET_URLS = {
            "/reviews/my",
    };

    // 조회(GET)만 비로그인 허용. 같은 경로의 생성·수정·삭제는 로그인이 필요하다.
    public static final String[] PUBLIC_GET_URLS = {
            // reviews
            "/reviews/list", "/reviews/list/**", "/reviews/*",

            // product
            "/products", "/products/**",
    };
}
