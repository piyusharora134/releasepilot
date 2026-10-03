package com.releasepilot.feature;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTargetingRuleRequest {
    private Integer priority;

    @NotBlank(message = "Attribute is required")
    private String attribute;

    @NotNull(message = "Operator is required")
    private RuleOperator operator;

    @NotEmpty(message = "Values are required")
    private List<String> values;

    @NotBlank(message = "Serve value is required")
    private String serveValue;
}
