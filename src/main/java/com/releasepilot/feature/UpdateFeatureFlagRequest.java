package com.releasepilot.feature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFeatureFlagRequest {
    private String name;
    private String description;
    private Boolean enabled;
    private String defaultServeValue;
}
