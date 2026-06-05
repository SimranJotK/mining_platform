package com.cryptomining.platform.service;

import com.cryptomining.platform.dto.AuditLogDto;
import com.cryptomining.platform.entity.AuditLog;
import com.cryptomining.platform.entity.User;
import com.cryptomining.platform.repository.AuditLogRepository;
import com.cryptomining.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void log(Long userId, String action, String resourceType, String resourceId,
                    AuditLog.AuditStatus status, Object details) {
        try {
            User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
            String ip = null;
            String userAgent = null;

            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ip = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
            }

            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ip)
                .userAgent(userAgent)
                .status(status)
                .details(details != null ? objectMapper.writeValueAsString(details) : null)
                .build();

            auditLogRepository.save(auditLog);
            log.info("AUDIT: user={} action={} status={}", userId, action, status);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    public Page<AuditLogDto> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findByOrderByCreatedAtDesc(pageable)
            .map(this::toDto);
    }

    public long countToday() {
        return auditLogRepository.count();
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
            .id(log.getId())
            .userId(log.getUser() != null ? log.getUser().getId() : null)
            .username(log.getUser() != null ? log.getUser().getUsername() : "SYSTEM")
            .action(log.getAction())
            .resourceType(log.getResourceType())
            .resourceId(log.getResourceId())
            .ipAddress(log.getIpAddress())
            .status(log.getStatus().name())
            .createdAt(log.getCreatedAt())
            .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
