package com.releasepilot.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogWriter {

    private final AuditLogRepository auditLogRepository;

    public void log(UUID userId, UUID organizationId, String entityType, UUID entityId, String action, String details) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .userId(userId)
                    .organizationId(organizationId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .detailsJson(details)
                    .build());
        } catch (Exception ignored) {
            // Audit failures must not block primary operations
        }
    }
}
