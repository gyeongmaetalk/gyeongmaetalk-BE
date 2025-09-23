package auctionTalk.auction.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 15 * 24 * 60 * 60 * 1000; // 15일

    /**
     * Refresh Token 저장
     */
    public void saveRefreshToken(Long memberId, String refreshToken) {
        String redisKey = generateKey(memberId);
        redisTemplate.opsForValue().set(
                redisKey,
                refreshToken,
                REFRESH_TOKEN_EXPIRE_TIME,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Refresh Token 조회
     */
    public String getRefreshToken(Long memberId) {
        return redisTemplate.opsForValue().get(
                generateKey(memberId)
        );
    }

    /**
     * Refresh Token 삭제
     */
    public void deleteRefreshToken(Long memberId) {
        redisTemplate.delete(generateKey(memberId));
    }

    /**
     * Redis 키 생성
     */
    private String generateKey(Long memberId) {
        return "refreshToken:member:" + memberId;
    }
}
