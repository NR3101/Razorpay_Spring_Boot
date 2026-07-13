package com.nr3101.razorpay.vault.dto.request;

import com.nr3101.razorpay.vault.validation.ExpiryYear;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

@Builder
public record TokenizeRequest(

        @NotBlank(message = "PAN is required")
        @LuhnCheck(message = "Invalid card number")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "PAN must be between 13 and 19 digits")
        String pan,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
        String cvv,

        @NotNull(message = "Expiry month is required")
        @Min(value = 1, message = "Expiry month must be between 1 and 12")
        @Max(value = 12, message = "Expiry month must be between 1 and 12")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @Min(value = 1000, message = "Expiry year must be a 4-digit year")
        @Max(value = 9999, message = "Expiry year must be a 4-digit year")
        @ExpiryYear // our custom annotation to validate that the expiry year is not in the past
        Integer expiryYear,

        UUID customerId,

        @Size(min = 3, max = 100, message = "Card holder name must be between 3 and 100 characters")
        String cardHolderName
) {
}
