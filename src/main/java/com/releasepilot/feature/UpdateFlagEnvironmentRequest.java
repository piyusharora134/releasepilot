package com.releasepilot.feature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFlagEnvironmentRequest {
    private Boolean enabled;
    private String serveValue;
}
