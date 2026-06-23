package com.nr3101.razorpay.payment.processor;

import com.nr3101.razorpay.common.enums.PaymentMethod;
import com.nr3101.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.nr3101.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        PaymentProcessor paymentProcessor = paymentProcessors.get(request.method());
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("No payment processor found for method: " + request.method());
        }

        return paymentProcessor.charge(request);
    }
}
