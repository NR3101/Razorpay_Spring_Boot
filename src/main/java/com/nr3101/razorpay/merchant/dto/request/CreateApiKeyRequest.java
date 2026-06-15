package com.nr3101.razorpay.merchant.dto.request;

import com.nr3101.razorpay.common.enums.Environment;

public record CreateApiKeyRequest(
    Environment environment
) {
}
