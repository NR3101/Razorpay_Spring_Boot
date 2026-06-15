package com.nr3101.razorpay.merchant.dto.response;

import com.nr3101.razorpay.common.enums.BusinessType;
import com.nr3101.razorpay.common.enums.MerchantStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
