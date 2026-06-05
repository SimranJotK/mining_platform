package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class MiningStatisticDto {
    private Long id;
    private Long workerId;
    private String workerName;
    private BigDecimal hashRate;
    private Long acceptedShares;
    private Long rejectedShares;
    private Long staleShares;
    private BigDecimal estimatedEarnings;
    private BigDecimal efficiency;
    private BigDecimal powerConsumption;
    private LocalDateTime recordedAt;
}
