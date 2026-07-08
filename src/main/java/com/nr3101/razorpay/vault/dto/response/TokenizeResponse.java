package com.nr3101.razorpay.vault.dto.response;

import com.nr3101.razorpay.common.enums.CardBrand;
import lombok.Builder;

@Builder
public record TokenizeResponse(

        String token,
        String lastFour,
        CardBrand brand,
        Integer expiryMonth,
        Integer expiryYear
) {
}
