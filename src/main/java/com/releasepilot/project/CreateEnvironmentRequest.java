package com.releasepilot.project;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEnvironmentRequest {
    @NotBlank(message = "Environment name is required")
    private String name;

    @NotBlank(message = "Environment key is required")
    private String key;
}
