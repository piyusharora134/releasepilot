package com.releasepilot.feature;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateRequest {
    @NotBlank(message = "Flag key is required")
    private String flagKey;

    private Map<String, Object> context;
}
