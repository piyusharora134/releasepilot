package com.releasepilot.feature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TargetingRuleDto {
    private UUID id;
    private UUID flagEnvironmentId;
    private Integer priority;
    private String attribute;
    private RuleOperator operator;
    private List<String> values;
    private String serveValue;
}
