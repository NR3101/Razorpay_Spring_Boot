package com.nr3101.razorpay.common.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-based implementation of IdempotencyStore. It uses Redis to store the state of idempotent requests.
 * The key is prefixed with "idempotency:" to avoid collisions with other Redis keys.
 * The value "__IN_PROGRESS__" is used to indicate that a request is currently being processed.
 * If Redis is unavailable, the implementation fails open, allowing requests to proceed without idempotency guarantees.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_PREFIX = "idempotency:";

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try {
            Boolean set = redisTemplate.opsForValue().setIfAbsent(REDIS_KEY_PREFIX + key, IN_PROGRESS, ttl);
            return Boolean.TRUE.equals(set);
        } catch (DataAccessException e) {
            log.warn("Idempotency store unavailable. Failing open for key: {}", key);
            return true;
        }
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + key, value, ttl);
        } catch (DataAccessException e) {
            log.warn("Failed to store value in Redis. Failing open for key: {}", key);
        }
    }

    @Override
    public Optional<String> get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
            return Optional.ofNullable(value);
        } catch (DataAccessException e) {
            log.warn("Failed to get value from Redis. Failing open for key: {}", key);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(REDIS_KEY_PREFIX + key);
        } catch (DataAccessException e) {
            log.warn("Failed to delete value from Redis. Failing open for key: {}", key);
        }
    }
}
