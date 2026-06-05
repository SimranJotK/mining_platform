package com.cryptomining.platform.controller;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.security.UserPrincipal;
import com.cryptomining.platform.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;
    private final SystemService systemService;
    private final NotificationService notificationService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        userService.suspendUser(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("User suspended", null));
    }

    @PostMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        userService.activateUser(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("User activated", null));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            auditService.getAuditLogs(PageRequest.of(page, size))));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<SystemHealthDto>> getPlatformAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(systemService.getSystemHealth()));
    }

    @PostMapping("/notifications/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcastNotification(
            @RequestBody Map<String, String> body) {
        userService.getAllUsers().forEach(user ->
            notificationService.createNotification(user.getId(),
                body.get("title"), body.get("message"),
                com.cryptomining.platform.entity.Notification.NotificationType.SYSTEM));
        return ResponseEntity.ok(ApiResponse.success("Notification broadcast", null));
    }
}
