package com.nr3101.razorpay.payment.service;

import com.nr3101.razorpay.payment.dto.request.PaymentInitRequest;
import com.nr3101.razorpay.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(UUID merchantId, PaymentInitRequest request);
}
