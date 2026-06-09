package com.nr3101.razorpay.operations.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing the association between a settlement and its related payments.
 * This entity captures the relationship between a settlement and the payments that are part of that settlement.
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "settlement_payment")
public class SettlementPayment {

    @EmbeddedId // Indicates that this field is an embedded primary key, which is a composite key consisting of settlementId and paymentId
    private SettlementPaymentId id; // Composite primary key consisting of settlementId and paymentId

    @MapsId() // Maps the settlementId part of the composite key to the settlement field, and doesn't require a separate column for settlementId in the settlement_payment table as it will use the settlementId from the composite key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;
}
