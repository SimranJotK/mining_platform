package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class MiningWorkerDto {
    private Long id;
    private String workerName;
    private String workerId;
    private String deviceType;
    private String status;
    private BigDecimal hashRate;
    private String hashRateUnit;
    private BigDecimal temperature;
    private BigDecimal powerConsumption;
    private Long uptimeSeconds;
    private String poolName;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
}
