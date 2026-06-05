package com.cryptomining.platform.repository;

import com.cryptomining.platform.entity.MiningPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MiningPoolRepository extends JpaRepository<MiningPool, Long> {
    List<MiningPool> findByStatus(MiningPool.PoolStatus status);
}
