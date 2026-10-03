package com.releasepilot.feature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvaluationResultDto {
    private String flagKey;
    private Object value;
    private EvaluationReason reason;
    private String environmentKey;
}
