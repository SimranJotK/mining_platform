package com.cryptomining.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mining_pools")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MiningPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_url", nullable = false)
    private String apiUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "pool_type")
    @Builder.Default
    private PoolType poolType = PoolType.BTC;

    @Column(name = "api_key_required")
    @Builder.Default
    private Boolean apiKeyRequired = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PoolStatus status = PoolStatus.ACTIVE;

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

    public enum PoolType { BTC, ETH, LTC, OTHER }
    public enum PoolStatus { ACTIVE, INACTIVE, MAINTENANCE }
}
