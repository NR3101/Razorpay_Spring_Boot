package com.nr3101.razorpay.payment.processor;

import com.nr3101.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.nr3101.razorpay.payment.processor.dto.response.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
