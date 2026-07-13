package com.nr3101.razorpay.merchant.service.impl;

import com.nr3101.razorpay.common.exception.ResourceNotFoundException;
import com.nr3101.razorpay.common.util.RandomizerUtil;
import com.nr3101.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.nr3101.razorpay.merchant.dto.response.ApiKeyResponse;
import com.nr3101.razorpay.merchant.dto.response.CreateApiKeyResponse;
import com.nr3101.razorpay.merchant.entity.ApiKey;
import com.nr3101.razorpay.merchant.entity.Merchant;
import com.nr3101.razorpay.merchant.mapper.ApiKeyMapper;
import com.nr3101.razorpay.merchant.repository.ApiKeyRepository;
import com.nr3101.razorpay.merchant.repository.MerchantRepository;
import com.nr3101.razorpay.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final ApiKeyMapper apiKeyMapper;

    private BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public CreateApiKeyResponse createApiKey(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "rzp_" + request.environment().name().toLowerCase() + "_" + RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40); // Generate a random secret key

        ApiKey apiKey = ApiKey.builder()
                .keyId(keyId)
                .keySecretHash(BCRYPT.encode(rawSecret)) // Store only the hash of the secret key for security
                .environment(request.environment())
                .merchant(merchant)
                .build();
        apiKey = apiKeyRepository.save(apiKey);

        return CreateApiKeyResponse.builder()
                .id(apiKey.getId())
                .keyId(keyId)
                // Return the raw secret only once, do not store it in plaintext
                .keySecret(rawSecret)
                .environment(apiKey.getEnvironment())
                .build();
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        log.info("Listing API keys for merchantId={}", merchantId);

        return apiKeyMapper.toResponseList(apiKeyRepository.findByMerchant_Id(merchantId));
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID apiKeyId) {
        log.info("Revoking API key with id={} for merchantId={}", apiKeyId, merchantId);

        ApiKey apiKey = apiKeyRepository.findByIdAndMerchant_Id(apiKeyId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("apiKey", apiKeyId));

        apiKey.setEnabled(false);
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public CreateApiKeyResponse rotateKey(UUID merchantId, UUID apiKeyId) {
        log.info("Rotating API key with id={} for merchantId={}", apiKeyId, merchantId);

        ApiKey apiKey = apiKeyRepository.findByIdAndMerchant_Id(apiKeyId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("apiKey", apiKeyId));

        if (!apiKey.isEnabled()) {
            throw new RuntimeException("Cannot rotate a revoked API key");
        }

        String newRawSecret = RandomizerUtil.randomBase64(40); // Generate a new random secret key
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash()); // Move current secret hash to previous
        apiKey.setKeySecretHash(BCRYPT.encode(newRawSecret)); // Update to new secret hash
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24)); // Set a grace period of 24 hours
        apiKey = apiKeyRepository.save(apiKey);

        return CreateApiKeyResponse.builder()
                .id(apiKey.getId())
                .keyId(apiKey.getKeyId())
                // Return the new raw secret only once, do not store it in plaintext
                .keySecret(newRawSecret)
                .environment(apiKey.getEnvironment())
                .build();
    }
}
