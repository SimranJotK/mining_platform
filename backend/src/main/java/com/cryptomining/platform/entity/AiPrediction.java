package com.cryptomining.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_predictions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private MiningWorker worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_type", nullable = false)
    private PredictionType predictionType;

    @Column(name = "prediction_data", columnDefinition = "JSON", nullable = false)
    private String predictionData;

    @Column(name = "confidence_score")
    private BigDecimal confidenceScore;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PredictionType {
        PROFIT_FORECAST, REVENUE_FORECAST, ANOMALY_DETECTION,
        WORKER_FAILURE, ENERGY_FORECAST, OPTIMIZATION, TREND_ANALYSIS
    }
}
