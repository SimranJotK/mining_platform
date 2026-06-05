package com.cryptomining.platform.repository;

import com.cryptomining.platform.entity.MiningWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MiningWorkerRepository extends JpaRepository<MiningWorker, Long> {
    List<MiningWorker> findByUserId(Long userId);
    List<MiningWorker> findByUserIdAndStatus(Long userId, MiningWorker.WorkerStatus status);
    long countByStatus(MiningWorker.WorkerStatus status);
}
