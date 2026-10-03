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
public class FlagEnvironmentDto {
    private UUID id;
    private UUID flagId;
    private UUID environmentId;
    private String environmentName;
    private String environmentKey;
    private Boolean enabled;
    private String serveValue;
    private List<TargetingRuleDto> targetingRules;
    private RolloutRuleDto rolloutRule;
    private Instant updatedAt;
}
