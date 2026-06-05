package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class AuditLogDto {
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String resourceType;
    private String resourceId;
    private String ipAddress;
    private String status;
    private Object details;
    private LocalDateTime createdAt;
}
