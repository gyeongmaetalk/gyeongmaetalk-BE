package auctionTalk.auction.domain.member.repository;

import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class CodeRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long EXPIRATION_TIME = 180; // 3분

    public void save(String code, Long memberId) {
        String key = buildKey(code);
        redisTemplate.opsForValue().set(key, memberId.toString(), EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public Long getMemberId(String code) {
        String key = buildKey(code);
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            throw new CustomApiException(ErrorCode.INVALID_AUTH_CODE);
        }

        redisTemplate.delete(key); // 일회성 코드니까 사용 후 삭제
        return Long.parseLong(value);
    }

    private String buildKey(String code) {
        return "auth:code:" + code;
    }
}