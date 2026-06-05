package com.cryptomining.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mining_api_integrations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MiningApiIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private MiningPool pool;

    @Lob
    @Column(name = "api_key_encrypted")
    private byte[] apiKeyEncrypted;

    @Column(name = "api_key_iv")
    private String apiKeyIv;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_mode")
    @Builder.Default
    private IntegrationMode integrationMode = IntegrationMode.SIMULATION;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum IntegrationMode { SIMULATION, API, CGMINER, BFGMINER, STRATUM }
}
