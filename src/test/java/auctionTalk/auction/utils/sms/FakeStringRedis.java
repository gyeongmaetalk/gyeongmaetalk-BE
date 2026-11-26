package auctionTalk.auction.utils.sms;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;

/** 만료 시간은 무시하고 값만 메모리 Map에 보관하는 StringRedisTemplate (테스트용) */
public final class FakeStringRedis {

    public final Map<String, String> store = new ConcurrentHashMap<>();
    public final StringRedisTemplate template = mock(StringRedisTemplate.class);

    @SuppressWarnings("unchecked")
    public FakeStringRedis() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        given(template.opsForValue()).willReturn(ops);

        willAnswer(inv -> store.put(inv.getArgument(0), inv.getArgument(1)))
                .given(ops).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        willAnswer(inv -> store.put(inv.getArgument(0), inv.getArgument(1)))
                .given(ops).set(anyString(), anyString(), any(Duration.class));
        given(ops.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willAnswer(inv -> store.putIfAbsent(inv.getArgument(0), inv.getArgument(1)) == null);
        given(ops.get(anyString())).willAnswer(inv -> store.get(inv.<String>getArgument(0)));
        given(ops.increment(anyString())).willAnswer(inv ->
                Long.parseLong(store.merge(inv.getArgument(0), "1", (a, b) -> String.valueOf(Long.parseLong(a) + 1))));
        given(template.delete(anyString())).willAnswer(inv -> store.remove(inv.<String>getArgument(0)) != null);
        given(template.expire(anyString(), any(Duration.class))).willReturn(true);
    }
}
