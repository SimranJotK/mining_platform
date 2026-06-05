package com.cryptomining.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mining_workers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MiningWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id")
    private MiningPool pool;

    @Column(name = "worker_name", nullable = false)
    private String workerName;

    @Column(name = "worker_id")
    private String workerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    @Builder.Default
    private DeviceType deviceType = DeviceType.SIMULATED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WorkerStatus status = WorkerStatus.OFFLINE;

    @Column(name = "hash_rate")
    @Builder.Default
    private BigDecimal hashRate = BigDecimal.ZERO;

    @Column(name = "hash_rate_unit")
    @Builder.Default
    private String hashRateUnit = "MH/s";

    private BigDecimal temperature;

    @Column(name = "power_consumption")
    private BigDecimal powerConsumption;

    @Column(name = "uptime_seconds")
    @Builder.Default
    private Long uptimeSeconds = 0L;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

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

    public enum DeviceType { GPU, ASIC, CPU, SIMULATED }
    public enum WorkerStatus { ONLINE, OFFLINE, IDLE, ERROR, MAINTENANCE }
}
