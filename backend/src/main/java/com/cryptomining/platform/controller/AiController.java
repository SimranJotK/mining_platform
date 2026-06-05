package com.cryptomining.platform.controller;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.entity.User;
import com.cryptomining.platform.repository.MiningStatisticRepository;
import com.cryptomining.platform.repository.UserRepository;
import com.cryptomining.platform.security.UserPrincipal;
import com.cryptomining.platform.service.AiServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiServiceClient aiService;
    private final UserRepository userRepository;
    private final MiningStatisticRepository statisticRepository;

    @GetMapping("/predictions")
    public ResponseEntity<ApiResponse<List<AiPredictionDto>>> getPredictions(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getPredictions(principal.getId())));
    }

    @PostMapping("/predictions/generate")
    public ResponseEntity<ApiResponse<List<AiPredictionDto>>> generatePredictions(
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId()).orElseThrow();
        var stats = statisticRepository.findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            principal.getId(), LocalDateTime.now().minusDays(7), LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(
            aiService.generatePredictions(principal.getId(), user, stats)));
    }
}
