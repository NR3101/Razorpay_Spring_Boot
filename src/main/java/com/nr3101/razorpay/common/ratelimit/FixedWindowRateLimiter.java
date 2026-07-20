package com.nr3101.razorpay.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This class implements a fixed window rate limiting algorithm using Redis as the backend store. It checks if the number of requests for a given key exceeds the maximum allowed requests within a specified time window. If the limit is exceeded, it returns a denial response with the retry-after time; otherwise, it allows the request and returns the remaining allowed requests.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "fixed")
@Slf4j
public class FixedWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    @Override
    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds) {

        String redisKey = "ratelimit:fixed:" + key;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        // if anyhow count is null(redis down etc.), we will allow max requests allowed by our system
        if (count == null) {
            return RateLimitResult.allowed(maxRequestAllowed);
        }

        // if this is the 1st req, then we will set the expiration for the key, so that it will be removed after windowSeconds
        if (count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }

        // if count exceeds maxRequestAllowed, we will deny the request and return the retryAfter time
        if (count > maxRequestAllowed) {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            int retryAfter = (ttl != null && ttl > 0) ? ttl.intValue() : (int) windowSeconds;
            return RateLimitResult.denied(retryAfter);
        }

        // if count is less than or equal to maxRequestAllowed, we will allow the request and return the remaining requests allowed
        return RateLimitResult.allowed(maxRequestAllowed - count.intValue());
    }
}
