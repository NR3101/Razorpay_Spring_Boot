package com.nr3101.razorpay.payment.entity;

import com.nr3101.razorpay.common.entity.BaseEntity;
import com.nr3101.razorpay.common.entity.Money;
import com.nr3101.razorpay.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_record",
        indexes = {
                @Index(name = "idx_order_id_merchant_id", columnList = "id, merchant_id"),
                @Index(name = "idx_order_merchant_id", columnList = "merchant_id")
        })
public class OrderRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // no FK as it is a cross domain reference, we will just store the merchantId as a UUID
    @Column(nullable = false) // Ensure that merchantId is not null
    private UUID merchantId;

    @Embedded // Used to indicate that Money is a value object and its fields should be mapped to the same table
    private Money amount;

    @Column(length = 100)
    private String receipt; // Optional field to store a receipt identifier i.e. a unique string that can be used to identify the order in the merchant's system

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED; // Default status when order is created

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0; // Number of attempts made to process the order

    @JdbcTypeCode(SqlTypes.JSON) // Converts the JSON blob to a Map<String, Object> and vice versa
    @Column(columnDefinition = "jsonb") //  Store the notes as a JSON object in the database
    private Map<String, Object> notes; // Additional info related to the order, stored as a JSON object

    @Column(nullable = false)
    private LocalDateTime expiresAt; // Time when the order expires
}
