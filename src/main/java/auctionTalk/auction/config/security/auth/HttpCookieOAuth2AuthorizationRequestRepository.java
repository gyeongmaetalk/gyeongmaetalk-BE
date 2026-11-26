package auctionTalk.auction.config.security.auth;

import auctionTalk.auction.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * OAuth2 인증 요청(state, redirect_uri 등)을 로그인 시작부터 콜백까지 보관한다.
 *
 * 요청 원문은 Redis에 두고, 쿠키에는 추측할 수 없는 식별자만 담는다.
 * - 클라이언트가 보낸 쿠키를 역직렬화하지 않는다. 역직렬화하는 값은 서버가 Redis에 쓴 것뿐이다.
 * - 콜백에서 꺼낼 때 조회와 삭제를 한 번에(Lua 스크립트) 처리해 같은 인증 요청을 두 번 쓸 수 없다.
 * - 쿠키가 인증 요청을 브라우저에 묶어 두므로, 다른 사람의 콜백 URL로 로그인시키는 공격(login CSRF)도 막는다.
 */
@Component
@RequiredArgsConstructor
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int EXPIRE_SECONDS = 180; // 3분
    private static final String KEY_PREFIX = "oauth2:auth-request:";
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{43}$"); // 32바이트 Base64URL
    private static final SecureRandom RANDOM = new SecureRandom();
    // GETDEL은 Redis 6.2부터라 버전과 무관하게 동작하도록 스크립트로 조회·삭제를 원자적으로 수행한다
    private static final RedisScript<String> GET_AND_DELETE = new DefaultRedisScript<>(
            "local v = redis.call('GET', KEYS[1]) if v then redis.call('DEL', KEYS[1]) end return v", String.class);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return resolveId(request)
                .map(id -> redisTemplate.opsForValue().get(KEY_PREFIX + id))
                .map(HttpCookieOAuth2AuthorizationRequestRepository::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            resolveId(request).ifPresent(id -> redisTemplate.delete(KEY_PREFIX + id));
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
            return;
        }

        String id = newId();
        redisTemplate.opsForValue().set(KEY_PREFIX + id, serialize(authorizationRequest), Duration.ofSeconds(EXPIRE_SECONDS));
        // 애플 로그인은 appleid.apple.com에서 form_post로 돌아오므로 SameSite=None이어야 콜백에 쿠키가 실린다
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, id, EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = resolveId(request)
                .map(id -> redisTemplate.execute(GET_AND_DELETE, List.of(KEY_PREFIX + id)))
                .map(HttpCookieOAuth2AuthorizationRequestRepository::deserialize)
                .orElse(null);
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        return authorizationRequest;
    }

    private static Optional<String> resolveId(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
                .map(Cookie::getValue)
                .filter(value -> ID_PATTERN.matcher(value).matches());
    }

    private static String newId() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // 서버가 직접 Redis에 쓴 값만 다루므로 Java 직렬화를 써도 된다 (OAuth2AuthorizationRequest는 Serializable)
    private static String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        return Base64.getEncoder().encodeToString(SerializationUtils.serialize(authorizationRequest));
    }

    private static OAuth2AuthorizationRequest deserialize(String value) {
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(Base64.getDecoder().decode(value));
    }
}
