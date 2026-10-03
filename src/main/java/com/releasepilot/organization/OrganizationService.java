package com.releasepilot.organization;

import com.releasepilot.audit.AuditLogWriter;
import com.releasepilot.common.DuplicateResourceException;
import com.releasepilot.common.ResourceNotFoundException;
import com.releasepilot.user.User;
import com.releasepilot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final AuditLogWriter auditLogWriter;

    @Transactional
    public OrganizationDto createOrganization(UUID userId, CreateOrganizationRequest request) {
        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Organization slug already exists: " + request.getSlug());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Organization organization = Organization.builder()
                .name(request.getName())
                .slug(request.getSlug().toLowerCase())
                .build();

        Organization savedOrg = organizationRepository.save(organization);

        OrganizationMember member = OrganizationMember.builder()
                .user(user)
                .organization(savedOrg)
                .role(OrganizationRole.OWNER)
                .build();

        organizationMemberRepository.save(member);

        auditLogWriter.log(userId, savedOrg.getId(), "ORGANIZATION", savedOrg.getId(), "CREATE",
                "Created organization: " + savedOrg.getSlug());

        return mapToDto(savedOrg, OrganizationRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getUserOrganizations(UUID userId) {
        List<OrganizationMember> members = organizationMemberRepository.findByUserId(userId);
        return members.stream()
                .map(m -> mapToDto(m.getOrganization(), m.getRole()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganizationDto getOrganizationBySlug(UUID userId, String slug) {
        Organization org = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "slug", slug));

        OrganizationMember member = organizationMemberRepository.findByUserIdAndOrganizationId(userId, org.getId())
                .orElseThrow(() -> new ResourceNotFoundException("OrganizationMember", "orgId", org.getId()));

        return mapToDto(org, member.getRole());
    }

    private OrganizationDto mapToDto(Organization org, OrganizationRole role) {
        return OrganizationDto.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .userRole(role)
                .build();
    }
}
