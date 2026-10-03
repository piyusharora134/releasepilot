package com.releasepilot.project;

import com.releasepilot.audit.AuditLogWriter;
import com.releasepilot.common.DuplicateResourceException;
import com.releasepilot.common.ResourceNotFoundException;
import com.releasepilot.organization.Organization;
import com.releasepilot.organization.OrganizationMemberRepository;
import com.releasepilot.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final EnvironmentRepository environmentRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final AuditLogWriter auditLogWriter;

    @Transactional
    public ProjectDto createProject(UUID userId, CreateProjectRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        if (!organizationMemberRepository.existsByUserIdAndOrganizationId(userId, org.getId())) {
            throw new ResourceNotFoundException("Member does not belong to organization");
        }

        String projectKey = request.getKey().toLowerCase().trim();
        if (projectRepository.existsByOrganizationIdAndKey(org.getId(), projectKey)) {
            throw new DuplicateResourceException("Project key already exists in this organization: " + projectKey);
        }

        Project project = Project.builder()
                .organization(org)
                .name(request.getName())
                .key(projectKey)
                .description(request.getDescription())
                .build();

        Project savedProject = projectRepository.save(project);

        // Auto-create standard default environments for the project
        createDefaultEnvironment(savedProject, "Development", "dev");
        createDefaultEnvironment(savedProject, "Staging", "staging");
        createDefaultEnvironment(savedProject, "Production", "prod");

        auditLogWriter.log(userId, org.getId(), "PROJECT", savedProject.getId(), "CREATE",
                "Created project: " + projectKey + " with default environments (dev, staging, prod)");

        return getProjectById(savedProject.getId());
    }

    private void createDefaultEnvironment(Project project, String name, String key) {
        Environment env = Environment.builder()
                .project(project)
                .name(name)
                .key(key)
                .apiKey("rp_sdk_" + key + "_" + UUID.randomUUID().toString().replace("-", ""))
                .build();
        environmentRepository.save(env);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByOrganization(UUID organizationId) {
        List<Project> projects = projectRepository.findByOrganizationId(organizationId);
        return projects.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return mapToDto(project);
    }

    @Transactional
    public EnvironmentDto createEnvironment(UUID userId, UUID projectId, CreateEnvironmentRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String envKey = request.getKey().toLowerCase().trim();
        if (environmentRepository.existsByProjectIdAndKey(projectId, envKey)) {
            throw new DuplicateResourceException("Environment key already exists in this project: " + envKey);
        }

        Environment environment = Environment.builder()
                .project(project)
                .name(request.getName())
                .key(envKey)
                .apiKey("rp_sdk_" + envKey + "_" + UUID.randomUUID().toString().replace("-", ""))
                .build();

        Environment savedEnv = environmentRepository.save(environment);

        auditLogWriter.log(userId, project.getOrganization().getId(), "ENVIRONMENT", savedEnv.getId(), "CREATE",
                "Created environment: " + envKey + " in project " + project.getKey());

        return mapToEnvDto(savedEnv);
    }

    @Transactional
    public EnvironmentDto regenerateApiKey(UUID userId, UUID environmentId) {
        Environment env = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Environment", "id", environmentId));

        env.setApiKey("rp_sdk_" + env.getKey() + "_" + UUID.randomUUID().toString().replace("-", ""));
        Environment updated = environmentRepository.save(env);

        auditLogWriter.log(userId, env.getProject().getOrganization().getId(), "ENVIRONMENT", updated.getId(),
                "REGENERATE_KEY", "Regenerated SDK API key for environment: " + env.getKey());

        return mapToEnvDto(updated);
    }

    public ProjectDto mapToDto(Project project) {
        List<Environment> envs = environmentRepository.findByProjectId(project.getId());
        List<EnvironmentDto> envDtos = envs.stream().map(this::mapToEnvDto).collect(Collectors.toList());

        return ProjectDto.builder()
                .id(project.getId())
                .organizationId(project.getOrganization().getId())
                .name(project.getName())
                .key(project.getKey())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .environments(envDtos)
                .build();
    }

    public EnvironmentDto mapToEnvDto(Environment env) {
        return EnvironmentDto.builder()
                .id(env.getId())
                .projectId(env.getProject().getId())
                .name(env.getName())
                .key(env.getKey())
                .apiKey(env.getApiKey())
                .createdAt(env.getCreatedAt())
                .updatedAt(env.getUpdatedAt())
                .build();
    }
}
