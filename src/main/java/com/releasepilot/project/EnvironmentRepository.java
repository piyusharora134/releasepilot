package com.releasepilot.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, UUID> {
    List<Environment> findByProjectId(UUID projectId);
    Optional<Environment> findByProjectIdAndKey(UUID projectId, String key);
    Optional<Environment> findByApiKey(String apiKey);
    boolean existsByProjectIdAndKey(UUID projectId, String key);
}
