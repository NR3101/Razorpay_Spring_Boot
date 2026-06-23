package com.nr3101.razorpay.payment.gateway;

import com.nr3101.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.nr3101.razorpay.payment.gateway.dto.response.PaymentResult;

public interface PaymentAdapter {

    PaymentResult initiate(PaymentRequest request);
}
