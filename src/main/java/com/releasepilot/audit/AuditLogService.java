package com.releasepilot.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAuditLogsByOrganization(UUID organizationId) {
        List<AuditLog> logs = auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);
        return logs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private AuditLogDto mapToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .organizationId(log.getOrganizationId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .detailsJson(log.getDetailsJson())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
