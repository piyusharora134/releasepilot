package com.releasepilot.feature;

import com.releasepilot.common.ApiResponse;
import com.releasepilot.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flags")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeatureFlagDto>> createFeatureFlag(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateFeatureFlagRequest request) {
        FeatureFlagDto flag = featureFlagService.createFeatureFlag(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Feature flag created successfully", flag));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeatureFlagDto>>> getFeatureFlagsByProject(
            @RequestParam UUID projectId) {
        List<FeatureFlagDto> flags = featureFlagService.getFeatureFlagsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(flags));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureFlagDto>> getFeatureFlagById(@PathVariable UUID id) {
        FeatureFlagDto flag = featureFlagService.getFeatureFlagById(id);
        return ResponseEntity.ok(ApiResponse.success(flag));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureFlagDto>> updateFeatureFlag(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateFeatureFlagRequest request) {
        FeatureFlagDto flag = featureFlagService.updateFeatureFlag(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Feature flag updated", flag));
    }

    @PatchMapping("/environments/{flagEnvironmentId}")
    public ResponseEntity<ApiResponse<FlagEnvironmentDto>> updateFlagEnvironment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID flagEnvironmentId,
            @RequestBody UpdateFlagEnvironmentRequest request) {
        FlagEnvironmentDto flagEnv = featureFlagService.updateFlagEnvironment(principal.getId(), flagEnvironmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Environment status updated", flagEnv));
    }

    @PostMapping("/environments/{flagEnvironmentId}/targeting-rules")
    public ResponseEntity<ApiResponse<TargetingRuleDto>> addTargetingRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID flagEnvironmentId,
            @Valid @RequestBody CreateTargetingRuleRequest request) {
        TargetingRuleDto rule = featureFlagService.addTargetingRule(principal.getId(), flagEnvironmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Targeting rule added", rule));
    }

    @DeleteMapping("/targeting-rules/{ruleId}")
    public ResponseEntity<ApiResponse<Void>> deleteTargetingRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID ruleId) {
        featureFlagService.deleteTargetingRule(principal.getId(), ruleId);
        return ResponseEntity.ok(ApiResponse.success("Targeting rule deleted", null));
    }

    @PostMapping("/environments/{flagEnvironmentId}/rollout-rules")
    public ResponseEntity<ApiResponse<RolloutRuleDto>> setRolloutRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID flagEnvironmentId,
            @Valid @RequestBody CreateRolloutRuleRequest request) {
        RolloutRuleDto rule = featureFlagService.setRolloutRule(principal.getId(), flagEnvironmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Rollout rule configured", rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeatureFlag(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        featureFlagService.deleteFeatureFlag(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Feature flag deleted", null));
    }
}
