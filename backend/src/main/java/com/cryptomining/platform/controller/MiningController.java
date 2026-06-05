package com.cryptomining.platform.controller;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.entity.MiningPool;
import com.cryptomining.platform.entity.User;
import com.cryptomining.platform.repository.UserRepository;
import com.cryptomining.platform.security.UserPrincipal;
import com.cryptomining.platform.service.MiningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mining")
@RequiredArgsConstructor
public class MiningController {

    private final MiningService miningService;
    private final UserRepository userRepository;

    @GetMapping("/workers")
    public ResponseEntity<ApiResponse<List<MiningWorkerDto>>> getWorkers(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(miningService.getWorkers(principal.getId())));
    }

    @PostMapping("/workers")
    public ResponseEntity<ApiResponse<MiningWorkerDto>> createWorker(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(principal.getId()).orElseThrow();
        String name = (String) body.get("workerName");
        Long poolId = body.get("poolId") != null ? ((Number) body.get("poolId")).longValue() : null;
        return ResponseEntity.ok(ApiResponse.success(
            miningService.createWorker(principal.getId(), user, name, poolId)));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<MiningStatisticDto>>> getStatistics(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(ApiResponse.success(
            miningService.getStatistics(principal.getId(), hours)));
    }

    @GetMapping("/pools")
    public ResponseEntity<ApiResponse<List<MiningPool>>> getPools() {
        return ResponseEntity.ok(ApiResponse.success(miningService.getPools()));
    }
}
