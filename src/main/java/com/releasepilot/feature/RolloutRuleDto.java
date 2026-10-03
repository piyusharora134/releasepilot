package com.releasepilot.feature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RolloutRuleDto {
    private UUID id;
    private UUID flagEnvironmentId;
    private String attribute;
    private Integer percentage;
    private String serveValueA;
    private String serveValueB;
}
