package com.nr3101.razorpay.merchant.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisApiKeyCache implements ApiKeyCache {

    private static final String REDIS_KEY_PREFIX = "apikey:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + keyId);
            if (json == null) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(json, ApiKeyCacheEntry.class));
        } catch (Exception e) {
            log.warn("Could not get api key cache entry from redis for apiKey:{}, exception occurred: {}", keyId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String keyId, ApiKeyCacheEntry entry) {
        try {
            String value = objectMapper.writeValueAsString(entry);
            stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX + keyId, value, TTL);
        } catch (Exception e) {
            log.warn("Could not put api key cache entry to redis for apiKey:{}, exception occurred: {}", keyId, e.getMessage());
        }
    }

    @Override
    public void evict(String keyId) {
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + keyId);
    }
}
