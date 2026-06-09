package com.nr3101.razorpay.operations.entity;

import jakarta.persistence.Embeddable;

import java.util.UUID;

/**
 * Embeddable class representing the composite primary key for the SettlementPayment entity.
 * This class encapsulates the settlementId and paymentId fields which together form the composite key.
 */

@Embeddable
public class SettlementPaymentId {

    private UUID settlementId; // Foreign key to the Settlement entity

    private UUID paymentId; // ID of the payment associated with this settlement payment
}
