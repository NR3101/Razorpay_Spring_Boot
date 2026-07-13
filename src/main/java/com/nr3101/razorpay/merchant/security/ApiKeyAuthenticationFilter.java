package com.nr3101.razorpay.merchant.security;

import com.nr3101.razorpay.merchant.entity.ApiKey;
import com.nr3101.razorpay.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder BCRYT = new BCryptPasswordEncoder();
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("Incoming request: {}", request.getRequestURI());

            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith(BASIC_PREFIX)) {
                log.warn("Missing or invalid Authorization header");
                filterChain.doFilter(request, response);
                return;
            }

            String[] credentials = decode(header);
            if (credentials == null) {
                throw new BadRequestException("Malformed API Key  header");
            }

            String keyId = credentials[0];
            String rawSecret = credentials[1];

            ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
                    .orElseThrow(() -> new BadRequestException("Invalid or missing API Key"));

            if (!apiKey.isEnabled() || !secretMatches(rawSecret, apiKey)) {
                throw new BadRequestException("Invalid or missing API Key");
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    keyId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_API_KEY"))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            merchantContext.setMerchantId(apiKey.getMerchant().getId());
            merchantContext.setKeyId(apiKey.getKeyId());

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("Error during API Key authentication: {}", e.getMessage(), e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private boolean secretMatches(String rawSecret, ApiKey apiKey) {
        if (BCRYT.matches(rawSecret, apiKey.getKeySecretHash())) {
            return true;
        }

        boolean isInGracePeriod = apiKey.getGracePeriodExpiresAt() != null && LocalDateTime.now().isBefore(apiKey.getGracePeriodExpiresAt());

        return isInGracePeriod &&
                apiKey.getPreviousKeySecretHash() != null &&
                BCRYT.matches(rawSecret, apiKey.getPreviousKeySecretHash());
    }

    private String[] decode(String header) {
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        int colon = decoded.indexOf(':');
        if (colon < 1) return null;

        return new String[]{decoded.substring(0, colon), decoded.substring(colon + 1)};
    }
}
