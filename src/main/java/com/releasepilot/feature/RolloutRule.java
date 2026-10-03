package com.releasepilot.feature;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rollout_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolloutRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_environment_id", nullable = false)
    private FlagEnvironment flagEnvironment;

    @Builder.Default
    @Column(nullable = false, length = 100)
    private String attribute = "userId";

    @Column(nullable = false)
    private Integer percentage;

    @Column(name = "serve_value_a", nullable = false, columnDefinition = "TEXT")
    private String serveValueA;

    @Column(name = "serve_value_b", nullable = false, columnDefinition = "TEXT")
    private String serveValueB;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (attribute == null) attribute = "userId";
    }
}
