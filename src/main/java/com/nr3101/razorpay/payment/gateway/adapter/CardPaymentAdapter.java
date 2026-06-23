package com.nr3101.razorpay.payment.gateway.adapter;

import com.nr3101.razorpay.payment.gateway.PaymentAdapter;
import com.nr3101.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.nr3101.razorpay.payment.gateway.dto.response.PaymentResult;

public class CardPaymentAdapter implements PaymentAdapter {

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        return null;
    }
}
