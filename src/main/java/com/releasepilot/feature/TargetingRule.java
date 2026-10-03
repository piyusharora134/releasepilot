package com.releasepilot.feature;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "targeting_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_environment_id", nullable = false)
    private FlagEnvironment flagEnvironment;

    @Builder.Default
    @Column(nullable = false)
    private Integer priority = 0;

    @Column(nullable = false, length = 100)
    private String attribute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RuleOperator operator;

    @Column(name = "values_json", nullable = false, columnDefinition = "TEXT")
    private String valuesJson;

    @Column(name = "serve_value", nullable = false, columnDefinition = "TEXT")
    private String serveValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (priority == null) priority = 0;
    }
}
