package com.cryptomining.platform.service;

import com.cryptomining.platform.dto.SystemHealthDto;
import com.cryptomining.platform.entity.SystemConfiguration;
import com.cryptomining.platform.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserService userService;
    private final MiningService miningService;
    private final AuditService auditService;
    private final AiServiceClient aiServiceClient;
    private final SystemConfigurationRepository configRepository;

    public SystemHealthDto getSystemHealth() {
        String aiStatus = aiServiceClient.checkHealth();
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("uptime", System.currentTimeMillis());
        metrics.put("jvm_memory_used",
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        metrics.put("jvm_memory_max", Runtime.getRuntime().maxMemory());

        return SystemHealthDto.builder()
            .status("HEALTHY")
            .backendStatus("UP")
            .databaseStatus("UP")
            .aiServiceStatus(aiStatus)
            .totalUsers(userService.countTotalUsers())
            .activeUsers(userService.countActiveUsers())
            .totalWorkers(miningService.countTotalWorkers())
            .onlineWorkers(miningService.countOnlineWorkers())
            .auditLogsToday(auditService.countToday())
            .metrics(metrics)
            .build();
    }

    public List<SystemConfiguration> getConfigurations() {
        return configRepository.findAll();
    }

    public SystemConfiguration updateConfiguration(String key, String value, Long updatedBy) {
        SystemConfiguration config = configRepository.findByConfigKey(key)
            .orElse(SystemConfiguration.builder().configKey(key).build());
        config.setConfigValue(value);
        return configRepository.save(config);
    }
}
