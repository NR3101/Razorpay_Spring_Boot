package com.nr3101.razorpay.merchant.service;

import com.nr3101.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.nr3101.razorpay.merchant.dto.response.ApiKeyResponse;
import com.nr3101.razorpay.merchant.dto.response.CreateApiKeyResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {

    CreateApiKeyResponse createApiKey(UUID merchantId, @Valid CreateApiKeyRequest request);

    List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID apiKeyId);

    CreateApiKeyResponse rotateKey(UUID merchantId, UUID apiKeyId);
}
