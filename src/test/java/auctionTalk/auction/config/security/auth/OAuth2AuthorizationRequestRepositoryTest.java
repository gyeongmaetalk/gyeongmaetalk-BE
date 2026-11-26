package auctionTalk.auction.config.security.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static auctionTalk.auction.config.security.auth.HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;

class OAuth2AuthorizationRequestRepositoryTest {

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository = newRepository();
        Gadget.DESERIALIZED.set(false);
    }

    // Redis 대신 메모리 Map으로 동작하는 RedisTemplate
    private final Map<String, String> store = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private HttpCookieOAuth2AuthorizationRequestRepository newRepository() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(ops);
        willAnswer(inv -> store.put(inv.getArgument(0), inv.getArgument(1))).given(ops).set(anyString(), anyString(), any(Duration.class));
        given(ops.get(anyString())).willAnswer(inv -> store.get(inv.<String>getArgument(0)));
        given(redisTemplate.execute(any(RedisScript.class), anyList()))
                .willAnswer(inv -> store.remove(inv.<List<String>>getArgument(1).get(0)));
        given(redisTemplate.delete(anyString())).willAnswer(inv -> store.remove(inv.<String>getArgument(0)) != null);
        return new HttpCookieOAuth2AuthorizationRequestRepository(redisTemplate);
    }

    @Test
    @DisplayName("저장한 인증 요청을 콜백에서 다시 꺼낼 수 있다")
    void saveAndLoad() {
        OAuth2AuthorizationRequest saved = authorizationRequest("state-1");
        String cookieValue = save(saved);

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(callback(cookieValue));

        assertThat(loaded).isNotNull();
        assertThat(loaded.getState()).isEqualTo("state-1");
        assertThat(loaded.getRedirectUri()).isEqualTo(saved.getRedirectUri());
    }

    @Test
    @DisplayName("공격자가 만든 쿠키 값은 역직렬화되지 않는다 (기존: 클라이언트 쿠키를 그대로 Java 역직렬화)")
    void craftedCookieIsNeverDeserialized() {
        String crafted = Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(new Gadget()));

        try {
            repository.loadAuthorizationRequest(callback(crafted));
        } catch (RuntimeException ignored) {
            // 기존 코드는 역직렬화 후 타입 캐스팅에서 ClassCastException이 난다. 이미 readObject가 실행된 뒤다.
        }

        assertThat(Gadget.DESERIALIZED).as("클라이언트가 보낸 바이트로 객체가 만들어졌다").isFalse();
    }

    @Test
    @DisplayName("쿠키에는 인증 요청 원문이 아니라 추측할 수 없는 식별자만 담긴다")
    void cookieHoldsOnlyAnOpaqueId() {
        String cookieValue = save(authorizationRequest("state-2"));

        assertThat(cookieValue).doesNotContain("state-2");
        assertThat(cookieValue.length()).isLessThanOrEqualTo(64);
    }

    @Test
    @DisplayName("콜백에서 꺼낸 인증 요청은 한 번만 쓸 수 있다 (기존: remove가 조회만 하고 지우지 않음)")
    void removeConsumesRequest() {
        String cookieValue = save(authorizationRequest("state-3"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthorizationRequest first = repository.removeAuthorizationRequest(callback(cookieValue), response);
        OAuth2AuthorizationRequest second = repository.removeAuthorizationRequest(callback(cookieValue), new MockHttpServletResponse());

        assertThat(first).isNotNull();
        assertThat(second).as("같은 쿠키로 다시 꺼내면 안 된다").isNull();
        assertThat(response.getHeaders("Set-Cookie"))
                .anyMatch(h -> h.startsWith(OAUTH2_AUTH_REQUEST_COOKIE_NAME + "=") && h.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("저장 기간이 지나 Redis에서 사라진 요청은 꺼낼 수 없다")
    void expiredRequest() {
        String cookieValue = save(authorizationRequest("state-4"));
        store.clear();

        assertThat(repository.removeAuthorizationRequest(callback(cookieValue), new MockHttpServletResponse())).isNull();
    }

    @Test
    @DisplayName("쿠키가 없거나 모르는 식별자면 null")
    void unknownCookie() {
        assertThat(repository.loadAuthorizationRequest(new MockHttpServletRequest())).isNull();
        assertThat(repository.loadAuthorizationRequest(callback("unknown-id"))).isNull();
        assertThat(repository.loadAuthorizationRequest(callback("A".repeat(43)))).isNull(); // 형식은 맞지만 저장된 적 없는 식별자
    }

    // ---------- helpers ----------

    private String save(OAuth2AuthorizationRequest authorizationRequest) {
        MockHttpServletResponse response = new MockHttpServletResponse();
        repository.saveAuthorizationRequest(authorizationRequest, new MockHttpServletRequest(), response);
        Cookie cookie = response.getCookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        assertThat(cookie).isNotNull();
        return cookie.getValue();
    }

    private static MockHttpServletRequest callback(String cookieValue) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login/oauth2/code/apple");
        request.setCookies(new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, cookieValue));
        return request;
    }

    private static OAuth2AuthorizationRequest authorizationRequest(String state) {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .clientId("client")
                .redirectUri("https://api.example.com/login/oauth2/code/apple")
                .state(state)
                .build();
    }

    /** readObject가 실행되면 표시만 남기는 무해한 클래스. 실제 공격에서는 이 자리에 클래스패스의 가젯 체인이 온다. */
    static class Gadget implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        static final AtomicBoolean DESERIALIZED = new AtomicBoolean(false);

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            DESERIALIZED.set(true);
        }
    }
}
