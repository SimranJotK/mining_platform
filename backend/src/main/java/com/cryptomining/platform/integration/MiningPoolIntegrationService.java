package com.cryptomining.platform.integration;

import com.cryptomining.platform.entity.MiningPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class MiningPoolIntegrationService {

    private final WebClient webClient;

    public MiningPoolIntegrationService() {
        this.webClient = WebClient.builder()
            .codecs(config -> config.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }

    public Map<String, Object> fetchPoolStats(MiningPool pool, String apiKey) {
        if (pool.getName().equals("Simulation Pool")) {
            return generateSimulatedStats();
        }
        try {
            return webClient.get()
                .uri(pool.getApiUrl())
                .header("Authorization", apiKey != null ? "Bearer " + apiKey : "")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .block();
        } catch (Exception e) {
            log.warn("Pool API fetch failed for {}: {}", pool.getName(), e.getMessage());
            return generateSimulatedStats();
        }
    }

    public Map<String, Object> generateSimulatedStats() {
        Random random = new Random();
        Map<String, Object> stats = new HashMap<>();
        stats.put("hashrate", BigDecimal.valueOf(100 + random.nextDouble() * 200));
        stats.put("accepted_shares", random.nextInt(10000));
        stats.put("rejected_shares", random.nextInt(100));
        stats.put("workers_online", random.nextInt(5) + 1);
        stats.put("workers_offline", random.nextInt(2));
        stats.put("estimated_earnings", BigDecimal.valueOf(random.nextDouble() * 0.01));
        stats.put("pool_efficiency", 95.0 + random.nextDouble() * 4);
        stats.put("mode", "SIMULATION");
        return stats;
    }

    public List<Map<String, Object>> fetchWorkerStats(MiningPool pool, String apiKey) {
        Map<String, Object> poolStats = fetchPoolStats(pool, apiKey);
        List<Map<String, Object>> workers = new ArrayList<>();
        int count = (int) poolStats.getOrDefault("workers_online", 3);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            Map<String, Object> worker = new HashMap<>();
            worker.put("name", "worker-" + (i + 1));
            worker.put("hashrate", BigDecimal.valueOf(30 + random.nextDouble() * 70));
            worker.put("status", random.nextDouble() > 0.15 ? "ONLINE" : "OFFLINE");
            worker.put("temperature", 55 + random.nextDouble() * 20);
            worker.put("accepted", random.nextInt(5000));
            worker.put("rejected", random.nextInt(50));
            workers.add(worker);
        }
        return workers;
    }
}
