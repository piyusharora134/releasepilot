package com.releasepilot.organization;

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
public class OrganizationDto {
    private UUID id;
    private String name;
    private String slug;
    private Instant createdAt;
    private Instant updatedAt;
    private OrganizationRole userRole;
}
