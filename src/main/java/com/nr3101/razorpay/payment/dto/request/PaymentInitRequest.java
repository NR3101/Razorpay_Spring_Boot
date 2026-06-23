package com.nr3101.razorpay.payment.dto.request;

import com.nr3101.razorpay.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record PaymentInitRequest(

        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        Map<String, Object> methodDetails
) {
}
