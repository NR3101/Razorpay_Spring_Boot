package com.nr3101.razorpay.payment.dto.response;

import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.common.enums.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID id,
        UUID merchantId,
        String receipt,
        Money amount,
        OrderStatus status,
        Integer attempts,
        Map<String,Object> notes,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
