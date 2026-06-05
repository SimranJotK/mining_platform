package com.cryptomining.platform.service;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.entity.*;
import com.cryptomining.platform.exception.ResourceNotFoundException;
import com.cryptomining.platform.integration.MiningPoolIntegrationService;
import com.cryptomining.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MiningService {

    private final MiningWorkerRepository workerRepository;
    private final MiningStatisticRepository statisticRepository;
    private final MiningPoolRepository poolRepository;
    private final NotificationRepository notificationRepository;
    private final MiningPoolIntegrationService poolIntegration;
    private final Random random = new Random();

    @Value("${app.mining.simulation-enabled:true}")
    private boolean simulationEnabled;

    public DashboardSummaryDto getDashboardSummary(Long userId) {
        List<MiningWorker> workers = workerRepository.findByUserId(userId);
        int online = (int) workers.stream()
            .filter(w -> w.getStatus() == MiningWorker.WorkerStatus.ONLINE).count();

        BigDecimal totalHash = workers.stream()
            .map(MiningWorker::getHashRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        List<MiningStatistic> stats = statisticRepository
            .findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, dayAgo, LocalDateTime.now());

        long accepted = stats.stream().mapToLong(MiningStatistic::getAcceptedShares).sum();
        long rejected = stats.stream().mapToLong(MiningStatistic::getRejectedShares).sum();
        BigDecimal earnings = stats.stream()
            .map(MiningStatistic::getEstimatedEarnings)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgEfficiency = stats.isEmpty() ? BigDecimal.ZERO :
            stats.stream().map(MiningStatistic::getEfficiency)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(stats.size()), 2, RoundingMode.HALF_UP);

        BigDecimal power = workers.stream()
            .map(w -> w.getPowerConsumption() != null ? w.getPowerConsumption() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryDto.builder()
            .totalWorkers(workers.size())
            .onlineWorkers(online)
            .offlineWorkers(workers.size() - online)
            .totalHashRate(totalHash)
            .hashRateUnit("MH/s")
            .totalAcceptedShares(accepted)
            .totalRejectedShares(rejected)
            .estimatedDailyEarnings(earnings)
            .averageEfficiency(avgEfficiency)
            .totalPowerConsumption(power)
            .recentWorkers(workers.stream().limit(5).map(this::toWorkerDto).collect(Collectors.toList()))
            .unreadNotifications((int) notificationRepository.countByUserIdAndIsReadFalse(userId))
            .build();
    }

    public List<MiningWorkerDto> getWorkers(Long userId) {
        return workerRepository.findByUserId(userId).stream()
            .map(this::toWorkerDto).collect(Collectors.toList());
    }

    public List<MiningStatisticDto> getStatistics(Long userId, int hours) {
        LocalDateTime start = LocalDateTime.now().minusHours(hours);
        return statisticRepository
            .findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, start, LocalDateTime.now())
            .stream().map(this::toStatisticDto).collect(Collectors.toList());
    }

    public List<MiningPool> getPools() {
        return poolRepository.findByStatus(MiningPool.PoolStatus.ACTIVE);
    }

    @Transactional
    public MiningWorkerDto createWorker(Long userId, User user, String name, Long poolId) {
        MiningPool pool = poolId != null ? poolRepository.findById(poolId).orElse(null) : null;
        MiningWorker worker = MiningWorker.builder()
            .user(user)
            .pool(pool)
            .workerName(name)
            .workerId("WRK-" + System.currentTimeMillis())
            .deviceType(MiningWorker.DeviceType.SIMULATED)
            .status(MiningWorker.WorkerStatus.OFFLINE)
            .build();
        return toWorkerDto(workerRepository.save(worker));
    }

    @Transactional
    public void simulateMiningData(Long userId) {
        if (!simulationEnabled) return;

        List<MiningWorker> workers = workerRepository.findByUserId(userId);
        if (workers.isEmpty()) return;

        User user = workers.get(0).getUser();
        for (MiningWorker worker : workers) {
            BigDecimal hashRate = BigDecimal.valueOf(50 + random.nextDouble() * 150)
                .setScale(2, RoundingMode.HALF_UP);
            boolean online = random.nextDouble() > 0.1;

            worker.setHashRate(hashRate);
            worker.setStatus(online ? MiningWorker.WorkerStatus.ONLINE : MiningWorker.WorkerStatus.OFFLINE);
            worker.setTemperature(BigDecimal.valueOf(55 + random.nextDouble() * 25).setScale(1, RoundingMode.HALF_UP));
            worker.setPowerConsumption(BigDecimal.valueOf(800 + random.nextDouble() * 400).setScale(0, RoundingMode.HALF_UP));
            worker.setUptimeSeconds(worker.getUptimeSeconds() + 60);
            worker.setLastSeenAt(LocalDateTime.now());
            workerRepository.save(worker);

            if (online) {
                statisticRepository.save(MiningStatistic.builder()
                    .worker(worker)
                    .user(user)
                    .hashRate(hashRate)
                    .acceptedShares((long) (random.nextInt(10) + 1))
                    .rejectedShares((long) random.nextInt(2))
                    .staleShares((long) random.nextInt(1))
                    .estimatedEarnings(BigDecimal.valueOf(random.nextDouble() * 0.001).setScale(8, RoundingMode.HALF_UP))
                    .efficiency(BigDecimal.valueOf(92 + random.nextDouble() * 7).setScale(2, RoundingMode.HALF_UP))
                    .powerConsumption(worker.getPowerConsumption())
                    .recordedAt(LocalDateTime.now())
                    .build());
            }
        }
    }

    public long countOnlineWorkers() {
        return workerRepository.countByStatus(MiningWorker.WorkerStatus.ONLINE);
    }

    public long countTotalWorkers() {
        return workerRepository.count();
    }

    private MiningWorkerDto toWorkerDto(MiningWorker w) {
        return MiningWorkerDto.builder()
            .id(w.getId())
            .workerName(w.getWorkerName())
            .workerId(w.getWorkerId())
            .deviceType(w.getDeviceType().name())
            .status(w.getStatus().name())
            .hashRate(w.getHashRate())
            .hashRateUnit(w.getHashRateUnit())
            .temperature(w.getTemperature())
            .powerConsumption(w.getPowerConsumption())
            .uptimeSeconds(w.getUptimeSeconds())
            .poolName(w.getPool() != null ? w.getPool().getName() : null)
            .lastSeenAt(w.getLastSeenAt())
            .createdAt(w.getCreatedAt())
            .build();
    }

    private MiningStatisticDto toStatisticDto(MiningStatistic s) {
        return MiningStatisticDto.builder()
            .id(s.getId())
            .workerId(s.getWorker().getId())
            .workerName(s.getWorker().getWorkerName())
            .hashRate(s.getHashRate())
            .acceptedShares(s.getAcceptedShares())
            .rejectedShares(s.getRejectedShares())
            .staleShares(s.getStaleShares())
            .estimatedEarnings(s.getEstimatedEarnings())
            .efficiency(s.getEfficiency())
            .powerConsumption(s.getPowerConsumption())
            .recordedAt(s.getRecordedAt())
            .build();
    }
}
