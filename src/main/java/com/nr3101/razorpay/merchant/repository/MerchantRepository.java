package com.nr3101.razorpay.merchant.repository;

import com.nr3101.razorpay.merchant.entity.Merchant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByEmail(String email);
}