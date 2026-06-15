package com.nr3101.razorpay.merchant.service;

import com.nr3101.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.nr3101.razorpay.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);
}
