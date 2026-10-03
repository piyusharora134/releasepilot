package com.releasepilot.feature;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFeatureFlagRequest {
    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Flag key is required")
    private String key;

    @NotBlank(message = "Flag name is required")
    private String name;

    private String description;

    @NotNull(message = "Flag type is required")
    private FlagType flagType;

    @NotBlank(message = "Default serve value is required")
    private String defaultServeValue;
}
