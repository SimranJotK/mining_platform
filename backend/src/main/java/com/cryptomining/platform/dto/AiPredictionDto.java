package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class AiPredictionDto {
    private Long id;
    private String predictionType;
    private Object predictionData;
    private BigDecimal confidenceScore;
    private Long workerId;
    private String workerName;
    private LocalDateTime validUntil;
    private LocalDateTime createdAt;
}
