package com.nr3101.razorpay.merchant.repository;

import com.nr3101.razorpay.merchant.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    List<ApiKey> findByMerchant_Id(UUID merchantId);

    Optional<ApiKey> findByIdAndMerchant_Id(UUID apiKeyId, UUID merchantId);
}