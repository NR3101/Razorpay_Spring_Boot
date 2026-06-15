package com.nr3101.razorpay.merchant.dto.response;

import com.nr3101.razorpay.common.enums.Environment;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateApiKeyResponse(
        UUID id,
        String keyId,
        String keySecret,
        Environment environment
) {
}
