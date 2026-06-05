package com.cryptomining.platform.controller;

import com.cryptomining.platform.dto.ApiResponse;
import com.cryptomining.platform.dto.SystemHealthDto;
import com.cryptomining.platform.entity.SystemConfiguration;
import com.cryptomining.platform.security.UserPrincipal;
import com.cryptomining.platform.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/creator")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CREATOR')")
public class CreatorController {

    private final SystemService systemService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<SystemHealthDto>> getHealth() {
        return ResponseEntity.ok(ApiResponse.success(systemService.getSystemHealth()));
    }

    @GetMapping("/configurations")
    public ResponseEntity<ApiResponse<List<SystemConfiguration>>> getConfigurations() {
        return ResponseEntity.ok(ApiResponse.success(systemService.getConfigurations()));
    }

    @PutMapping("/configurations/{key}")
    public ResponseEntity<ApiResponse<SystemConfiguration>> updateConfiguration(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
            systemService.updateConfiguration(key, body.get("value"), principal.getId())));
    }

    @PostMapping("/deploy")
    public ResponseEntity<ApiResponse<Map<String, String>>> deployService(
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "status", "DEPLOYMENT_INITIATED",
            "service", body.getOrDefault("service", "unknown"),
            "message", "Deployment queued for processing"
        )));
    }
}
