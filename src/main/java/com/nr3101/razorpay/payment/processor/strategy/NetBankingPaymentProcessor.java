package com.nr3101.razorpay.payment.processor.strategy;

import com.nr3101.razorpay.payment.processor.PaymentProcessor;
import com.nr3101.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.nr3101.razorpay.payment.processor.dto.response.PaymentProcessorResponse;

public class NetBankingPaymentProcessor implements PaymentProcessor {
    
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        return null;
    }
}
