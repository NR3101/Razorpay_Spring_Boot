package com.nr3101.razorpay.merchant.controller;

import com.nr3101.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.nr3101.razorpay.merchant.dto.response.ApiKeyResponse;
import com.nr3101.razorpay.merchant.dto.response.CreateApiKeyResponse;
import com.nr3101.razorpay.merchant.security.MerchantContext;
import com.nr3101.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> createApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.createApiKey(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listByMerchant() {
        return ResponseEntity.ok(apiKeyService.listByMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{apiKeyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID apiKeyId) {
        apiKeyService.revoke(merchantContext.getMerchantId(), apiKeyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{apiKeyId}/rotate")
    public ResponseEntity<CreateApiKeyResponse> rotateKey(@PathVariable UUID apiKeyId) {
        return ResponseEntity.ok(
                apiKeyService.rotateKey(merchantContext.getMerchantId(), apiKeyId)
        );
    }
}
