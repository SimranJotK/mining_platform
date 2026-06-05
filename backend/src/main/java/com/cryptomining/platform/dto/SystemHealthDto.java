package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data @Builder
public class SystemHealthDto {
    private String status;
    private String backendStatus;
    private String databaseStatus;
    private String aiServiceStatus;
    private long totalUsers;
    private long activeUsers;
    private long totalWorkers;
    private long onlineWorkers;
    private long auditLogsToday;
    private Map<String, Object> metrics;
}
