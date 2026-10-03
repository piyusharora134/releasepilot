package com.releasepilot.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EvaluationLogRepository extends JpaRepository<EvaluationLog, UUID> {
    List<EvaluationLog> findByEnvironmentIdAndFlagKeyOrderByEvaluatedAtDesc(UUID environmentId, String flagKey);
    
    long countByEnvironmentIdAndFlagKey(UUID environmentId, String flagKey);

    @Query("SELECT e.resultValue, COUNT(e) FROM EvaluationLog e WHERE e.environmentId = :environmentId AND e.flagKey = :flagKey GROUP BY e.resultValue")
    List<Object[]> countByResultValueForFlag(UUID environmentId, String flagKey);
}
