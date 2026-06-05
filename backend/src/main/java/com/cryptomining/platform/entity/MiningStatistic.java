package com.cryptomining.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mining_statistics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MiningStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private MiningWorker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hash_rate", nullable = false)
    private BigDecimal hashRate;

    @Column(name = "accepted_shares")
    @Builder.Default
    private Long acceptedShares = 0L;

    @Column(name = "rejected_shares")
    @Builder.Default
    private Long rejectedShares = 0L;

    @Column(name = "stale_shares")
    @Builder.Default
    private Long staleShares = 0L;

    @Column(name = "estimated_earnings")
    @Builder.Default
    private BigDecimal estimatedEarnings = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal efficiency = BigDecimal.ZERO;

    @Column(name = "power_consumption")
    private BigDecimal powerConsumption;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
