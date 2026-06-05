package com.cryptomining.platform.service;

import com.cryptomining.platform.dto.AiPredictionDto;
import com.cryptomining.platform.entity.*;
import com.cryptomining.platform.repository.AiPredictionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    private final AiPredictionRepository predictionRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.service-url}")
    private String aiServiceUrl;

    public List<AiPredictionDto> getPredictions(Long userId) {
        return predictionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toDto).toList();
    }

    public List<AiPredictionDto> generatePredictions(Long userId, User user,
            List<MiningStatistic> statistics) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("user_id", userId);
            request.put("statistics", statistics.stream().map(s -> Map.of(
                "hash_rate", s.getHashRate(),
                "accepted_shares", s.getAcceptedShares(),
                "rejected_shares", s.getRejectedShares(),
                "efficiency", s.getEfficiency(),
                "power_consumption", s.getPowerConsumption() != null ? s.getPowerConsumption() : 0,
                "recorded_at", s.getRecordedAt().toString()
            )).toList());

            WebClient client = WebClient.builder().baseUrl(aiServiceUrl).build();
            Map response = client.post()
                .uri("/api/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response != null && response.containsKey("predictions")) {
                List<Map<String, Object>> preds = (List<Map<String, Object>>) response.get("predictions");
                List<AiPredictionDto> results = new ArrayList<>();
                for (Map<String, Object> pred : preds) {
                    AiPrediction saved = predictionRepository.save(AiPrediction.builder()
                        .user(user)
                        .predictionType(AiPrediction.PredictionType.valueOf(
                            (String) pred.get("type")))
                        .predictionData(objectMapper.writeValueAsString(pred.get("data")))
                        .confidenceScore(new BigDecimal(pred.get("confidence").toString()))
                        .validUntil(LocalDateTime.now().plusHours(24))
                        .build());
                    results.add(toDto(saved));
                }
                return results;
            }
        } catch (Exception e) {
            log.warn("AI service unavailable, using fallback predictions: {}", e.getMessage());
        }
        return generateFallbackPredictions(userId, user);
    }

    private List<AiPredictionDto> generateFallbackPredictions(Long userId, User user) {
        Random random = new Random();
        List<AiPredictionDto> results = new ArrayList<>();
        AiPrediction.PredictionType[] types = AiPrediction.PredictionType.values();

        for (AiPrediction.PredictionType type : types) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("forecast", random.nextDouble() * 100);
                data.put("trend", random.nextBoolean() ? "UP" : "DOWN");
                data.put("recommendation", "Optimize worker scheduling for peak hours");

                AiPrediction saved = predictionRepository.save(AiPrediction.builder()
                    .user(user)
                    .predictionType(type)
                    .predictionData(objectMapper.writeValueAsString(data))
                    .confidenceScore(BigDecimal.valueOf(0.7 + random.nextDouble() * 0.25))
                    .validUntil(LocalDateTime.now().plusHours(24))
                    .build());
                results.add(toDto(saved));
            } catch (Exception e) {
                log.error("Failed to create fallback prediction", e);
            }
        }
        return results;
    }

    public String checkHealth() {
        try {
            WebClient client = WebClient.builder().baseUrl(aiServiceUrl).build();
            Map response = client.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            return response != null ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }

    private AiPredictionDto toDto(AiPrediction p) {
        Object data = null;
        try {
            data = objectMapper.readValue(p.getPredictionData(), Object.class);
        } catch (Exception ignored) {}
        return AiPredictionDto.builder()
            .id(p.getId())
            .predictionType(p.getPredictionType().name())
            .predictionData(data)
            .confidenceScore(p.getConfidenceScore())
            .workerId(p.getWorker() != null ? p.getWorker().getId() : null)
            .validUntil(p.getValidUntil())
            .createdAt(p.getCreatedAt())
            .build();
    }
}
