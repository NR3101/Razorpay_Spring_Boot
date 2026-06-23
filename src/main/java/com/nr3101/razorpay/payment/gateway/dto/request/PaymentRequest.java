package com.nr3101.razorpay.payment.gateway.dto.request;

import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.common.enums.PaymentMethod;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record PaymentRequest(

        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
