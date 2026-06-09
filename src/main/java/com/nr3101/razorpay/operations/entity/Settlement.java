package com.nr3101.razorpay.operations.entity;

import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.common.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Embedded
    // Used to override the default column names for the fields of the embedded Money class in the settlement table otherwise it would have created columns named amountUnits and currency which would be ambiguous if we have multiple Money fields in the same entity
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gross_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "gross_amount_currency", nullable = false)
            )})
    private Money grossAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "refund_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "refund_amount_currency")
            )})
    private Money refundAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gst_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "gst_amount_currency")
            )})
    private Money gstAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "fee_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "fee_amount_currency")
            )})
    private Money feeAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "net_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "net_amount_currency", nullable = false)
            )})
    private Money netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(nullable = false, length = 100)
    private String bankReference;

    private LocalDateTime processedAt;
}
