package com.releasepilot.feature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TargetingRuleRepository extends JpaRepository<TargetingRule, UUID> {
    List<TargetingRule> findByFlagEnvironmentIdOrderByPriorityAsc(UUID flagEnvironmentId);
    void deleteByFlagEnvironmentId(UUID flagEnvironmentId);
}
