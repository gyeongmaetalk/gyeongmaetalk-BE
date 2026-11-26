package auctionTalk.auction.config;

import auctionTalk.auction.utils.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    private final RedisCacheManager cacheManager =
            new RedisConfig().redisCacheManager(mock(RedisConnectionFactory.class));

    @Test
    @DisplayName("목록 캐시는 presigned URL보다 먼저 만료된다 (만료된 이미지 URL을 응답하지 않음)")
    void cacheExpiresBeforePresignedUrl() {
        for (String name : List.of("reviewList", "reviewCursorList", "counselorReviewList", "myReviewList", "counselorImages")) {
            Duration ttl = ((RedisCache) cacheManager.getCache(name)).getCacheConfiguration().getTtl();

            assertThat(ttl).as(name).isPositive();
            assertThat(ttl).as(name).isLessThan(S3Service.PRESIGNED_GET_URL_TTL);
        }
    }
}
