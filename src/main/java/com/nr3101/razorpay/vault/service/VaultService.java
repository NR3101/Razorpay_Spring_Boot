package com.nr3101.razorpay.vault.service;

import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import com.nr3101.razorpay.vault.dto.request.TokenizeRequest;
import com.nr3101.razorpay.vault.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {

    TokenizeResponse tokenize(TokenizeRequest tokenizeRequest, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
