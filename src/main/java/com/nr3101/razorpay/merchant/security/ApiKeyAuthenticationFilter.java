package com.nr3101.razorpay.merchant.security;

import com.nr3101.razorpay.common.exception.RateLimitException;
import com.nr3101.razorpay.common.ratelimit.RateLimitResult;
import com.nr3101.razorpay.common.ratelimit.RateLimiter;
import com.nr3101.razorpay.merchant.cache.ApiKeyCache;
import com.nr3101.razorpay.merchant.cache.ApiKeyCacheEntry;
import com.nr3101.razorpay.merchant.entity.ApiKey;
import com.nr3101.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Filter that authenticates requests using API keys. It checks the Authorization header for a valid API key and secret, verifies them against the database, and applies rate limiting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;

    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:60}")
    private int maxRequestsPerMinute;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("Incoming request: {}", request.getRequestURI());

            // Check for the Authorization header
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith(BASIC_PREFIX)) {
                log.warn("Missing or invalid Authorization header");
                filterChain.doFilter(request, response);
                return;
            }

            // Decode the API key and secret from the Authorization header
            String[] credentials = decode(header);
            if (credentials == null) {
                throw new BadRequestException("Malformed API Key  header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            // Check the cache for the API key entry, or load it from the database if not present
            ApiKeyCacheEntry apiKeyEntry = apiKeyCache.get(keyId).
                    orElseGet(() -> loadAndCache(keyId));

            if (apiKeyEntry == null || !apiKeyEntry.enabled() || !secretMatches(rawSecret, apiKeyEntry)) {
                throw new BadRequestException("Invalid  or missing API Key");
            }

            // Apply rate limiting
            RateLimitResult rateLimitResult = rateLimiter.check("apikey:" + keyId, maxRequestsPerMinute, 60);

            if (!rateLimitResult.isAllowed()) {
                log.warn("Too many requests for API Key {}. Retry after {} seconds", keyId, rateLimitResult.retryAfterSeconds());
                throw new RateLimitException("Too many requests", rateLimitResult.retryAfterSeconds());
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));

            // Set the authentication in the security context and populate the merchant context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    keyId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_API_KEY"))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            merchantContext.setMerchantId(apiKeyEntry.merchantId());
            merchantContext.setKeyId(apiKeyEntry.keyId());

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("Error during API Key authentication: {}", e.getMessage(), e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    /**
     * Loads the ApiKey from the database and caches it.
     *
     * @param keyId The API key ID to load and cache.
     * @return The ApiKeyCacheEntry if found, otherwise null.
     */
    private ApiKeyCacheEntry loadAndCache(String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElse(null);
        if (apiKey == null) return null;

        ApiKeyCacheEntry apiKeyCacheEntry = ApiKeyCacheEntry.builder()
                .keyId(apiKey.getKeyId())
                .keySecretHash(apiKey.getKeySecretHash())
                .previousKeySecretHash(apiKey.getPreviousKeySecretHash())
                .gracePeriodExpiresAt(apiKey.getGracePeriodExpiresAt())
                .merchantId(apiKey.getMerchant().getId())
                .environment(apiKey.getEnvironment())
                .enabled(apiKey.isEnabled())
                .build();

        apiKeyCache.put(apiKey.getKeyId(), apiKeyCacheEntry);
        return apiKeyCacheEntry;
    }

    /**
     * Checks if the provided raw secret matches the stored secret hash or the previous secret hash (if in grace period).
     *
     * @param rawSecret The raw secret provided in the request.
     * @param apiKey    The ApiKeyCacheEntry containing the stored secret hashes.
     * @return True if the secret matches, otherwise false.
     */
    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry apiKey) {
        if (BCRYPT.matches(rawSecret, apiKey.keySecretHash())) {
            return true;
        }

        return apiKey.isInGracePeriod() &&
                apiKey.previousKeySecretHash() != null &&
                BCRYPT.matches(rawSecret, apiKey.previousKeySecretHash());
    }

    /**
     * Decodes the Basic Authorization header to extract the API key ID and secret.
     *
     * @param header The Authorization header value.
     * @return An array containing the API key ID and secret, or null if the header is malformed.
     */
    private String[] decode(String header) {
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(':');
        if (colon < 1) return null;

        return new String[]{decoded.substring(0, colon), decoded.substring(colon + 1)};
    }
}
