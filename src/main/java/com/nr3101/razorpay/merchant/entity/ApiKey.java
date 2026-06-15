package com.nr3101.razorpay.merchant.entity;

import com.nr3101.razorpay.common.enums.Environment;
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
@Table(name = "api_key")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, unique = true, length = 50)
    private String keyId;

    @Column(nullable = false, length = 100)
    private String keySecretHash; // Store only the hash of the secret key for security

    @Column(length = 100)
    private String previousKeySecretHash; // Store the hash of the previous secret key for rotation purposes

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Environment environment; // e.g., "TEST" or "PRODUCTION"

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    private LocalDateTime lastUsedAt;

    private LocalDateTime rotatedAt;

    private LocalDateTime gracePeriodExpiresAt;
}
