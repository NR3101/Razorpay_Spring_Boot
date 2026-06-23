package com.nr3101.razorpay.merchant.entity;

import com.nr3101.razorpay.common.entity.BaseEntity;
import com.nr3101.razorpay.common.enums.BusinessType;
import com.nr3101.razorpay.common.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "merchant",
        indexes = {
                @Index(name = "idx_merchant_status", columnList = "status"),
                @Index(name = "idx_merchant_email", columnList = "email")
        })
public class Merchant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String contactNumber;

    @Column(length = 100)
    private String businessName;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(length = 100)
    private String websiteUrl;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    @Builder.Default // Set default value when using builder
    private MerchantStatus status = MerchantStatus.PENDING_KYC;

    @Column(length = 20)
    private String gstId;

    @Column(length = 20)
    private String panId;

    @Column(length = 100)
    private String settlementBankAccount;

    @Column(length = 20)
    private String settlementBankIfsc;

    @Column(length = 100)
    private String settlementBankAccountHolderName;
}

