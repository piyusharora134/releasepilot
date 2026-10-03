package com.releasepilot.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDto {
    private UUID id;
    private UUID userId;
    private UUID organizationId;
    private String entityType;
    private UUID entityId;
    private String action;
    private String detailsJson;
    private Instant createdAt;
}
