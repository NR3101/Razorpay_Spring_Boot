package com.nr3101.razorpay.merchant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "merchant_webhook_config")
public class MerchantWebhookConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(length = 500, nullable = false)
    private String targetUrl;

    @Column(length = 255, nullable = false)
    private String webhookSecretHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(length = 1000)
    private String eventTypes; // Comma-separated list of event types to subscribe[e.g., "payment.captured, payment.failed"]
}
