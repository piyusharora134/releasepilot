package com.releasepilot.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByOrganizationId(UUID organizationId);
    Optional<Project> findByOrganizationIdAndKey(UUID organizationId, String key);
    boolean existsByOrganizationIdAndKey(UUID organizationId, String key);
}
