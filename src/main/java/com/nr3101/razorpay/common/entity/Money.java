package com.nr3101.razorpay.common.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable // Indicates that this class can be embedded in an entity i.e. all of its fields will be stored in the same table as the owning entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Money {

    private int amountUnits; // Amount in the smallest currency unit (e.g., cents for USD)
    private String currency; // Currency code (e.g., "USD", "INR")

    public static Money of(int amountUnits, String currency) {
        if (amountUnits < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (currency == null || currency.isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        return new Money(amountUnits, currency);
    }

    public static Money INR(int amountUnits) {
        return of(amountUnits, "INR");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add Money with different currencies");
        }
        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract Money with different currencies");
        }
        if (this.amountUnits < other.amountUnits) {
            throw new IllegalArgumentException("Resulting amount cannot be negative");
        }
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }
}
