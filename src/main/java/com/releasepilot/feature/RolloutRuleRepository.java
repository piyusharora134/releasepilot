package com.releasepilot.feature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolloutRuleRepository extends JpaRepository<RolloutRule, UUID> {
    Optional<RolloutRule> findByFlagEnvironmentId(UUID flagEnvironmentId);
    void deleteByFlagEnvironmentId(UUID flagEnvironmentId);
}
