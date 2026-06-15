package com.nr3101.razorpay.merchant.controller;

import com.nr3101.razorpay.merchant.dto.request.CreateApiKeyRequest;
import com.nr3101.razorpay.merchant.dto.response.ApiKeyResponse;
import com.nr3101.razorpay.merchant.dto.response.CreateApiKeyResponse;
import com.nr3101.razorpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/{merchantId}/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> createApiKey(@PathVariable UUID merchantId,
                                                             @Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.createApiKey(merchantId, request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listByMerchant(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(apiKeyService.listByMerchant(merchantId));
    }

    @DeleteMapping("/{apiKeyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID merchantId,
                                       @PathVariable UUID apiKeyId) {
        apiKeyService.revoke(merchantId, apiKeyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{apiKeyId}/rotate")
    public ResponseEntity<CreateApiKeyResponse> rotateKey(@PathVariable UUID merchantId,
                                       @PathVariable UUID apiKeyId) {
        return ResponseEntity.ok(
                apiKeyService.rotateKey(merchantId, apiKeyId)
        );
    }
}
