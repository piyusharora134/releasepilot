package com.releasepilot.feature;

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
public class FeatureFlagDto {
    private UUID id;
    private UUID projectId;
    private String key;
    private String name;
    private String description;
    private FlagType flagType;
    private String defaultServeValue;
    private Boolean enabled;
    private List<FlagEnvironmentDto> environments;
    private Instant createdAt;
    private Instant updatedAt;
}
