package com.cryptomining.platform.repository;

import com.cryptomining.platform.entity.MiningApiIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MiningApiIntegrationRepository extends JpaRepository<MiningApiIntegration, Long> {
    List<MiningApiIntegration> findByUserId(Long userId);
    List<MiningApiIntegration> findByIsActiveTrue();
}
