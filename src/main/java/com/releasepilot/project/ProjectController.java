package com.releasepilot.project;

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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectDto project = projectService.createProject(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Project created successfully", project));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getProjectsByOrganization(
            @RequestParam UUID organizationId) {
        List<ProjectDto> projects = projectService.getProjectsByOrganization(organizationId);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectById(@PathVariable UUID id) {
        ProjectDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PostMapping("/{projectId}/environments")
    public ResponseEntity<ApiResponse<EnvironmentDto>> createEnvironment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateEnvironmentRequest request) {
        EnvironmentDto env = projectService.createEnvironment(principal.getId(), projectId, request);
        return ResponseEntity.ok(ApiResponse.success("Environment created successfully", env));
    }

    @PostMapping("/environments/{environmentId}/regenerate-key")
    public ResponseEntity<ApiResponse<EnvironmentDto>> regenerateApiKey(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID environmentId) {
        EnvironmentDto env = projectService.regenerateApiKey(principal.getId(), environmentId);
        return ResponseEntity.ok(ApiResponse.success("API key regenerated", env));
    }
}
