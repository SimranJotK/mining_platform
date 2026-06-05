package com.cryptomining.platform.controller;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.security.UserPrincipal;
import com.cryptomining.platform.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MiningService miningService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
            miningService.getDashboardSummary(principal.getId())));
    }
}
