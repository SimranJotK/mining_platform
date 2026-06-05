package com.cryptomining.platform.repository;

import com.cryptomining.platform.entity.MiningStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MiningStatisticRepository extends JpaRepository<MiningStatistic, Long> {
    List<MiningStatistic> findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
        Long userId, LocalDateTime start, LocalDateTime end);
    List<MiningStatistic> findByWorkerIdAndRecordedAtBetweenOrderByRecordedAtAsc(
        Long workerId, LocalDateTime start, LocalDateTime end);

    MiningStatistic findFirstByUserIdOrderByRecordedAtDesc(Long userId);
}
