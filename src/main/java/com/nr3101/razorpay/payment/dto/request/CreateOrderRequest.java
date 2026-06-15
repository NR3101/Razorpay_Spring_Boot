package com.nr3101.razorpay.payment.dto.request;

import com.nr3101.razorpay.common.entity.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateOrderRequest(

        @NotNull(message = "Amount is required")
        Money amount,

        @Size(max = 100, message = "Receipt must be less than or equal to 100 characters")
        String receipt,

        Map<String, Object> notes,

        LocalDateTime expiresAt
) {
}
