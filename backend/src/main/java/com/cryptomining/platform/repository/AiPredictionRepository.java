package com.cryptomining.platform.repository;

import com.cryptomining.platform.entity.AiPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AiPredictionRepository extends JpaRepository<AiPrediction, Long> {
    List<AiPrediction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AiPrediction> findByUserIdAndPredictionType(Long userId, AiPrediction.PredictionType type);
}
