package com.nr3101.razorpay.vault.service.impl;

import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.common.enums.CardBrand;
import com.nr3101.razorpay.common.exception.ResourceNotFoundException;
import com.nr3101.razorpay.common.util.RandomizerUtil;
import com.nr3101.razorpay.payment.processor.PaymentProcessorRouter;
import com.nr3101.razorpay.payment.processor.dto.request.PaymentProcessorRequest;
import com.nr3101.razorpay.payment.processor.dto.response.PaymentProcessorResponse;
import com.nr3101.razorpay.vault.config.VaultEncryptionConfig;
import com.nr3101.razorpay.vault.dto.request.TokenizeRequest;
import com.nr3101.razorpay.vault.dto.response.TokenizeResponse;
import com.nr3101.razorpay.vault.entity.CardToken;
import com.nr3101.razorpay.vault.entity.VaultCard;
import com.nr3101.razorpay.vault.repository.CardTokenRepository;
import com.nr3101.razorpay.vault.repository.VaultCardRepository;
import com.nr3101.razorpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {
        log.info("Received request to tokenize card for merchantId: {}", merchantId);

        String lastFour = request.pan().substring(request.pan().length() - 4);
        String bin = request.pan().substring(0, 6);
        CardBrand brand = detectBrand(request.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncryptor(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = VaultCard.builder()
                .brand(brand)
                .lastFour(lastFour)
                .bin(bin)
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDek)
                .expiryMonth(request.expiryMonth().toString())
                .expiryYear(request.expiryYear().toString())
                .cardHolderName(request.cardHolderName())
                .build();
        vaultCard = vaultCardRepository.save(vaultCard);

        String token = RandomizerUtil.randomBase64(32);
        CardToken cardToken = CardToken.builder()
                .token(token)
                .vaultCard(vaultCard)
                .customer(request.customerId())
                .merchant(merchantId)
                .build();
        cardTokenRepository.save(cardToken);


        return TokenizeResponse.builder()
                .token(token)
                .lastFour(lastFour)
                .brand(brand)
                .expiryMonth(request.expiryMonth())
                .expiryYear(request.expiryYear())
                .build();
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        log.info("Received request to charge card for token: {}", token);

        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("CardToken", token));

        VaultCard vaultCard = cardToken.getVaultCard();
        byte[] panBytes = null;

        try {
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedPan());
            panBytes = VaultEncryptionConfig.panEncryptor(dek)
                    .decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.card(
                    paymentId,
                    pan,
                    expiry,
                    amount,
                    methodDetails
            );

            PaymentProcessorResponse response = paymentProcessorRouter.charge(paymentProcessorRequest);

            log.info("Charge card for token: {}****", token.substring(0, 4));

            return response;
        } catch (Exception e) {
            log.warn("Failed to charge card for token: {}****, error: {}", token.substring(0, 4), e.getMessage());
            return new PaymentProcessorResponse.Failure(
                    "VAULT_CHARGE_FAILED",
                    e.getMessage()
            );
        } finally {
            if (panBytes != null) {
                Arrays.fill(panBytes, (byte) 0); // Clear sensitive card data from memory
            }
        }
    }

    private CardBrand detectBrand(String pan) {
        if (pan.startsWith("4")) {
            return CardBrand.VISA;
        } else if (pan.startsWith("5") || pan.startsWith("2")) {
            return CardBrand.MASTERCARD;
        } else if (pan.startsWith("37") || pan.startsWith("34")) {
            return CardBrand.AMEX;
        }

        return CardBrand.RUPAY;
    }
}
