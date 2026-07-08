package com.nr3101.razorpay.payment.gateway;

import com.nr3101.razorpay.common.enums.PaymentMethod;
import com.nr3101.razorpay.payment.gateway.dto.request.PaymentRequest;
import com.nr3101.razorpay.payment.gateway.dto.response.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;

    public PaymentResult initiate(PaymentRequest request) {
        PaymentAdapter paymentAdapter = paymentAdapters.get(request.method());
        if (paymentAdapter == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + request.method());
        }

       return paymentAdapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapters.get(method);
        if (paymentAdapter == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }

        return paymentAdapter.capture(paymentId);
    }
}
