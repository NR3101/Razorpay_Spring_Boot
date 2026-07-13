package com.nr3101.razorpay.vault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to validate that the expiry year is not in the past.
 * Validated by {@link ExpiryYearValidator}.
 */
@Documented
@Constraint(validatedBy = {ExpiryYearValidator.class})
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface ExpiryYear {

    String message() default "Expiry year cannot be in past";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
