package com.releasepilot.feature;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRolloutRuleRequest {
    private String attribute;

    @NotNull(message = "Percentage is required")
    @Min(value = 0, message = "Percentage cannot be less than 0")
    @Max(value = 100, message = "Percentage cannot be greater than 100")
    private Integer percentage;

    @NotBlank(message = "Serve value A is required")
    private String serveValueA;

    @NotBlank(message = "Serve value B is required")
    private String serveValueB;
}
