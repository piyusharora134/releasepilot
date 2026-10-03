package com.releasepilot.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentDto {
    private UUID id;
    private UUID projectId;
    private String name;
    private String key;
    private String apiKey;
    private Instant createdAt;
    private Instant updatedAt;
}
