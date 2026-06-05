package com.cryptomining.platform.repository;

import com.cryptomining.platform.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByOrderByCreatedAtDesc(Pageable pageable);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(AuditLog.AuditStatus status);
}
