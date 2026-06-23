package com.nr3101.razorpay.payment.entity;

import com.nr3101.razorpay.common.entity.BaseEntity;
import com.nr3101.razorpay.common.enums.PaymentActor;
import com.nr3101.razorpay.common.enums.PaymentEvent;
import com.nr3101.razorpay.common.enums.PaymentStatus;
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
@Table(name = "payment_transaction_log",
        indexes = {
                @Index(name = "idx_payment_transaction_log_payment_id", columnList = "payment_id")
        })
public class PaymentTransactionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus fromStatus;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus toStatus;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentEvent event;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentActor actor; // e.g., "SYSTEM", "CUSTOMER", "MERCHANT"

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}
