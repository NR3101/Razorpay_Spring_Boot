package com.nr3101.razorpay.vault.controller;

import com.nr3101.razorpay.merchant.security.MerchantContext;
import com.nr3101.razorpay.vault.dto.request.TokenizeRequest;
import com.nr3101.razorpay.vault.dto.response.TokenizeResponse;
import com.nr3101.razorpay.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/vault")
@RequiredArgsConstructor
public class VaultController {

    private final MerchantContext merchantContext;
    private final VaultService vaultService;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest tokenizeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaultService.tokenize(tokenizeRequest, merchantContext.getMerchantId()));
    }
}
