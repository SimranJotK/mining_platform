package com.cryptomining.platform.controller;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.integration.MiningPoolIntegrationService;
import com.cryptomining.platform.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;
    private final MiningPoolIntegrationService poolIntegration;

    @GetMapping("/system/health")
    public ResponseEntity<ApiResponse<SystemHealthDto>> getHealth() {
        return ResponseEntity.ok(ApiResponse.success(systemService.getSystemHealth()));
    }

    @GetMapping("/simulation/pool")
    public ResponseEntity<Map<String, Object>> getSimulatedPoolStats() {
        return ResponseEntity.ok(poolIntegration.generateSimulatedStats());
    }
}
