package com.releasepilot.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evaluation_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "environment_id", nullable = false)
    private UUID environmentId;

    @Column(name = "flag_key", nullable = false, length = 100)
    private String flagKey;

    @Column(name = "user_context_id")
    private String userContextId;

    @Column(name = "user_context_json", columnDefinition = "TEXT")
    private String userContextJson;

    @Column(name = "result_value", nullable = false, columnDefinition = "TEXT")
    private String resultValue;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private Instant evaluatedAt;

    @PrePersist
    protected void onCreate() {
        if (evaluatedAt == null) evaluatedAt = Instant.now();
    }
}
