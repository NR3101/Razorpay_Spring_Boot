package com.nr3101.razorpay.vault.entity;

import com.nr3101.razorpay.common.entity.BaseEntity;
import com.nr3101.razorpay.common.enums.CardBrand;
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
@Table(name = "vault_card")
public class VaultCard extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 4, nullable = false)
    private String lastFour; // Last 4 digits of the card number for display purposes

    @Column(length = 6, nullable = false)
    private String bin; // Bank Identification Number (first 6 digits of the card)

    @Column(nullable = false)
    private byte[] encryptedPan; // Encrypted card details for security

    @Column(nullable = false)
    private byte[] encryptedDek; // Data Encryption Key for decrypting the card details

    @Column(nullable = false)
    private CardBrand brand; // Card brand (e.g., Visa, MasterCard)

    @Column(nullable = false)
    private String expiryMonth;

    @Column(nullable = false)
    private String expiryYear;

    @Column(nullable = false)
    private String cardHolderName;

    private LocalDateTime deletedAt; // Timestamp for soft deletion of the card
}

