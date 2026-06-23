package com.nr3101.razorpay.payment.processor.dto.request;

import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.common.enums.PaymentMethod;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record PaymentProcessorRequest(
        PaymentMethod method,
        Money amount,
        Map<String, Object> methodDetails
) {
}
