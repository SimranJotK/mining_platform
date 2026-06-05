package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class DashboardSummaryDto {
    private int totalWorkers;
    private int onlineWorkers;
    private int offlineWorkers;
    private BigDecimal totalHashRate;
    private String hashRateUnit;
    private Long totalAcceptedShares;
    private Long totalRejectedShares;
    private BigDecimal estimatedDailyEarnings;
    private BigDecimal averageEfficiency;
    private BigDecimal totalPowerConsumption;
    private List<MiningWorkerDto> recentWorkers;
    private int unreadNotifications;
}
