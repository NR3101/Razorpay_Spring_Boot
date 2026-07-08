package com.nr3101.razorpay.payment.processor.dto.request;

import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.common.enums.PaymentMethod;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record PaymentProcessorRequest(
        UUID processingId,
        UUID paymentId,
        PaymentMethod method,
        Money amount,
        String pan,
        String expiry,
        Map<String, Object> methodDetails
) {

    public static PaymentProcessorRequest card(UUID paymentId, String pan, String expiry, Money amount, Map<String, Object> details) {
        return PaymentProcessorRequest.builder()
                .processingId(UUID.randomUUID())
                .paymentId(paymentId)
                .method(PaymentMethod.CARD)
                .amount(amount)
                .pan(pan)
                .expiry(expiry)
                .methodDetails(details)
                .build();
    }

    public static PaymentProcessorRequest noncard(UUID paymentId, PaymentMethod method, Money amount, Map<String, Object> details) {
        return PaymentProcessorRequest.builder()
                .processingId(UUID.randomUUID())
                .paymentId(paymentId)
                .method(method)
                .amount(amount)
                .methodDetails(details)
                .build();
    }
}
