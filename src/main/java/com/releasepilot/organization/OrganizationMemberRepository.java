package com.releasepilot.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {
    List<OrganizationMember> findByUserId(UUID userId);
    List<OrganizationMember> findByOrganizationId(UUID organizationId);
    Optional<OrganizationMember> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
