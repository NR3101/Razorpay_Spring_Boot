package com.nr3101.razorpay.common.idempotency;

import com.nr3101.razorpay.common.exception.IdempotencyConflictException;
import com.nr3101.razorpay.merchant.security.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * IdempotencyFilter is a Spring filter that ensures idempotent behavior for HTTP requests with specific methods (POST, PUT, PATCH) and an "X-Idempotency-Key" header.
 * It uses an IdempotencyStore to track the state of requests and their responses.
 * <p>
 * The filter checks if a request with the same idempotency key is already in progress or has been completed. If so, it either replays the stored response or returns a conflict error.
 * If the request is new, it proceeds with the request and stores the response for future requests with the same key.
 * <p>
 * The filter is designed to work with a MerchantContext to differentiate idempotency keys across different merchants.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_METHODS = Set.of("POST", "PUT", "PATCH");
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);
    private static final String SEPARATOR = "|";

    private final MerchantContext merchantContext;
    private final IdempotencyStore idempotencyStore;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        if (!GUARDED_METHODS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader("X-Idempotency-Key");
        if (rawKey == null || rawKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        UUID merchantId = merchantContext.getMerchantId();
        String idempotencyKey = merchantId != null ? merchantId + ":" + rawKey : rawKey;

        boolean claimed = idempotencyStore.setIfAbsent(idempotencyKey, IN_PROGRESS_TTL);

        // claimed is false means another thread is already processing this request
        if (!claimed) {
            Optional<String> existingValue = idempotencyStore.get(idempotencyKey);

            // If the existing value is present and not equal to IN_PROGRESS, it means the request has already been processed and we can return the stored response.
            if (existingValue.isPresent() && !existingValue.get().equals(IdempotencyStore.IN_PROGRESS)) {
                replay(request, response, existingValue.get());
            } else {
                // Means another thread is processing this request, we can return exception
                var ex = new IdempotencyConflictException("Another request with the same idempotency key is already in progress. Please wait for it to complete.");
                handlerExceptionResolver.resolveException(request, response, null, ex);
            }
            return;
        }

        // if it's not yet claimed, we proceed with the request and store the response later in the filter chain.
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            // Store the response in the idempotency store for future requests with the same key.
            int status = wrappedResponse.getStatus();
            byte[] bodyBytes = wrappedResponse.getContentAsByteArray();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            if (status < 400 && !body.isBlank()) {
                // if the response is successful and not empty, we store it in the idempotency store.
                String storedValue = status + SEPARATOR + body;
                idempotencyStore.store(idempotencyKey, storedValue, COMPLETED_TTL);
            } else {
                // If the response is an error or empty, we delete the key so client can start fresh
                idempotencyStore.delete(idempotencyKey);
                log.debug("IdempotencyFilter: Deleted idempotency key: {} due to error status: {}", idempotencyKey, status);
            }

            // always copy the response body to the actual response, regardless of whether we stored it or not(else we will get empty response)
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * Replays the stored response for a given idempotency key. It extracts the status code and body from the stored value and writes them to the response.
     */
    private void replay(HttpServletRequest request, HttpServletResponse response, String storedValue) throws IOException {
        int separatorIndex = storedValue.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            var ex = new IdempotencyConflictException("Another request with the same idempotency key is already in progress. Please wait for it to complete.");
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }

        int status = Integer.parseInt(storedValue.substring(0, separatorIndex));
        String body = storedValue.substring(separatorIndex + 1);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}
