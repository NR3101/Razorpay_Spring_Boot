package com.nr3101.razorpay.merchant.dto.response;

import com.nr3101.razorpay.common.enums.Environment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ApiKeyResponse(
        UUID id,
        String keyId,
        Environment environment,
        boolean enabled,
        LocalDateTime lastUsedAt,
        LocalDateTime createdAt
) {
}
