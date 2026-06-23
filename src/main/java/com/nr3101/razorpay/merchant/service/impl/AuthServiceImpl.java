package com.nr3101.razorpay.merchant.service.impl;

import com.nr3101.razorpay.common.enums.MerchantStatus;
import com.nr3101.razorpay.common.enums.UserRole;
import com.nr3101.razorpay.common.exception.DuplicateResourceException;
import com.nr3101.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.nr3101.razorpay.merchant.dto.response.MerchantResponse;
import com.nr3101.razorpay.merchant.entity.AppUser;
import com.nr3101.razorpay.merchant.entity.Merchant;
import com.nr3101.razorpay.merchant.mapper.MerchantMapper;
import com.nr3101.razorpay.merchant.repository.AppUserRepository;
import com.nr3101.razorpay.merchant.repository.MerchantRepository;
import com.nr3101.razorpay.merchant.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        log.info("Received signup request for email: {}", request.email());

        if (merchantRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "DUPLICATE_MERCHANT_EMAIL",
                    "Merchant with email " + request.email() + " already exists"
            );
        }

        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC); // Set default status to PENDING_KYC
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .passwordHash(request.password()) //TODO: Hash the password later
                .merchant(merchant)
                .role(UserRole.OWNER)
                .build();
        appUserRepository.save(appUser);

        return merchantMapper.toResponse(merchant);
    }
}
