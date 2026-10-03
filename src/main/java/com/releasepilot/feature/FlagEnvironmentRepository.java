package com.releasepilot.feature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlagEnvironmentRepository extends JpaRepository<FlagEnvironment, UUID> {
    List<FlagEnvironment> findByFlagId(UUID flagId);
    List<FlagEnvironment> findByEnvironmentId(UUID environmentId);
    Optional<FlagEnvironment> findByFlagIdAndEnvironmentId(UUID flagId, UUID environmentId);
    Optional<FlagEnvironment> findByFlagKeyAndEnvironmentId(String flagKey, UUID environmentId);
}
