package com.nr3101.razorpay.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding")
@Slf4j
public class SlidingWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    @Override
    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds) {
        long nowMs = System.currentTimeMillis();
        long floorMs = nowMs - windowSeconds * 1000;

        String redisKey = "ratelimit:sliding:" + key;

        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
        zSet.removeRangeByScore(redisKey, Double.NEGATIVE_INFINITY, floorMs);

        Long count = zSet.zCard(redisKey);
        long currentCount = (count != null) ? count : 0;

        if (currentCount >= maxRequestAllowed) {
            Set<ZSetOperations.@NonNull TypedTuple<String>> oldest = zSet.rangeWithScores(redisKey, 0, 0);
            int retryAfter = 1;

            if (oldest != null && !oldest.isEmpty()) {
                Double oldestScore = oldest.iterator().next().getScore();
                if (oldestScore != null) {
                    long windowsExpireMs = oldestScore.longValue() * windowSeconds * 1000;
                    retryAfter = (int) Math.ceil((windowsExpireMs - nowMs) / 1000.0);
                }
            }
            return RateLimitResult.denied(retryAfter);
        }

        zSet.add(redisKey, UUID.randomUUID().toString(), nowMs);
        redisTemplate.expire(redisKey, windowSeconds + 1, TimeUnit.SECONDS);

        return RateLimitResult.allowed((int) (maxRequestAllowed - currentCount - 1));
    }
}
