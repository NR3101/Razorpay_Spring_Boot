package com.nr3101.razorpay.merchant.dto.request;

import com.nr3101.razorpay.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
        String businessName,

        BusinessType businessType
) {
}
