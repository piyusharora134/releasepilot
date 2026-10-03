package com.releasepilot.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String key;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private List<EnvironmentDto> environments;
}
