package com.releasepilot.feature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasepilot.audit.AuditLogWriter;
import com.releasepilot.common.DuplicateResourceException;
import com.releasepilot.common.ResourceNotFoundException;
import com.releasepilot.project.Environment;
import com.releasepilot.project.EnvironmentRepository;
import com.releasepilot.project.Project;
import com.releasepilot.project.ProjectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final FlagEnvironmentRepository flagEnvironmentRepository;
    private final TargetingRuleRepository targetingRuleRepository;
    private final RolloutRuleRepository rolloutRuleRepository;
    private final ProjectRepository projectRepository;
    private final EnvironmentRepository environmentRepository;
    private final AuditLogWriter auditLogWriter;
    private final ObjectMapper objectMapper;

    @Transactional
    public FeatureFlagDto createFeatureFlag(UUID userId, CreateFeatureFlagRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        String flagKey = request.getKey().toLowerCase().trim();
        if (featureFlagRepository.existsByProjectIdAndKey(project.getId(), flagKey)) {
            throw new DuplicateResourceException("Feature flag key already exists in this project: " + flagKey);
        }

        FeatureFlag flag = FeatureFlag.builder()
                .project(project)
                .key(flagKey)
                .name(request.getName())
                .description(request.getDescription())
                .flagType(request.getFlagType())
                .defaultServeValue(request.getDefaultServeValue())
                .enabled(true)
                .build();

        FeatureFlag savedFlag = featureFlagRepository.save(flag);

        // Auto-initialize flag states for all environments in this project
        List<Environment> envs = environmentRepository.findByProjectId(project.getId());
        for (Environment env : envs) {
            FlagEnvironment flagEnv = FlagEnvironment.builder()
                    .flag(savedFlag)
                    .environment(env)
                    .enabled(true)
                    .serveValue(request.getDefaultServeValue())
                    .build();
            flagEnvironmentRepository.save(flagEnv);
        }

        auditLogWriter.log(userId, project.getOrganization().getId(), "FEATURE_FLAG", savedFlag.getId(), "CREATE", "Created flag: " + flagKey);

        return getFeatureFlagById(savedFlag.getId());
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagDto> getFeatureFlagsByProject(UUID projectId) {
        List<FeatureFlag> flags = featureFlagRepository.findByProjectId(projectId);
        return flags.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeatureFlagDto getFeatureFlagById(UUID flagId) {
        FeatureFlag flag = featureFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("FeatureFlag", "id", flagId));
        return mapToDto(flag);
    }

    @Transactional
    public FeatureFlagDto updateFeatureFlag(UUID userId, UUID flagId, UpdateFeatureFlagRequest request) {
        FeatureFlag flag = featureFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("FeatureFlag", "id", flagId));

        if (request.getName() != null) {
            flag.setName(request.getName());
        }
        if (request.getDescription() != null) {
            flag.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            flag.setEnabled(request.getEnabled());
        }
        if (request.getDefaultServeValue() != null) {
            flag.setDefaultServeValue(request.getDefaultServeValue());
        }

        FeatureFlag updated = featureFlagRepository.save(flag);

        auditLogWriter.log(userId, flag.getProject().getOrganization().getId(), "FEATURE_FLAG", flagId, "UPDATE",
                "Updated flag: " + flag.getKey());

        return mapToDto(updated);
    }

    @Transactional
    public FlagEnvironmentDto updateFlagEnvironment(UUID userId, UUID flagEnvironmentId, UpdateFlagEnvironmentRequest request) {
        FlagEnvironment flagEnv = flagEnvironmentRepository.findById(flagEnvironmentId)
                .orElseThrow(() -> new ResourceNotFoundException("FlagEnvironment", "id", flagEnvironmentId));

        if (request.getEnabled() != null) {
            flagEnv.setEnabled(request.getEnabled());
        }
        if (request.getServeValue() != null) {
            flagEnv.setServeValue(request.getServeValue());
        }

        FlagEnvironment updated = flagEnvironmentRepository.save(flagEnv);

        auditLogWriter.log(userId, flagEnv.getFlag().getProject().getOrganization().getId(),
                "FLAG_ENVIRONMENT", updated.getId(), "TOGGLE",
                "Updated env " + flagEnv.getEnvironment().getKey() + " enabled=" + updated.getEnabled());

        return mapToFlagEnvDto(updated);
    }

    @Transactional
    public TargetingRuleDto addTargetingRule(UUID userId, UUID flagEnvironmentId, CreateTargetingRuleRequest request) {
        FlagEnvironment flagEnv = flagEnvironmentRepository.findById(flagEnvironmentId)
                .orElseThrow(() -> new ResourceNotFoundException("FlagEnvironment", "id", flagEnvironmentId));

        String valuesJson;
        try {
            valuesJson = objectMapper.writeValueAsString(request.getValues());
        } catch (Exception e) {
            valuesJson = "[]";
        }

        TargetingRule rule = TargetingRule.builder()
                .flagEnvironment(flagEnv)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .attribute(request.getAttribute())
                .operator(request.getOperator())
                .valuesJson(valuesJson)
                .serveValue(request.getServeValue())
                .build();

        TargetingRule savedRule = targetingRuleRepository.save(rule);

        auditLogWriter.log(userId, flagEnv.getFlag().getProject().getOrganization().getId(),
                "TARGETING_RULE", savedRule.getId(), "CREATE", "Added targeting rule for attr: " + request.getAttribute());

        return mapToTargetingRuleDto(savedRule);
    }

    @Transactional
    public void deleteTargetingRule(UUID userId, UUID ruleId) {
        TargetingRule rule = targetingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("TargetingRule", "id", ruleId));

        UUID orgId = rule.getFlagEnvironment().getFlag().getProject().getOrganization().getId();
        auditLogWriter.log(userId, orgId, "TARGETING_RULE", ruleId, "DELETE",
                "Deleted targeting rule for attribute: " + rule.getAttribute());

        targetingRuleRepository.delete(rule);
    }

    @Transactional
    public RolloutRuleDto setRolloutRule(UUID userId, UUID flagEnvironmentId, CreateRolloutRuleRequest request) {
        FlagEnvironment flagEnv = flagEnvironmentRepository.findById(flagEnvironmentId)
                .orElseThrow(() -> new ResourceNotFoundException("FlagEnvironment", "id", flagEnvironmentId));

        rolloutRuleRepository.deleteByFlagEnvironmentId(flagEnv.getId());

        RolloutRule rule = RolloutRule.builder()
                .flagEnvironment(flagEnv)
                .attribute(request.getAttribute() != null ? request.getAttribute() : "userId")
                .percentage(request.getPercentage())
                .serveValueA(request.getServeValueA())
                .serveValueB(request.getServeValueB())
                .build();

        RolloutRule saved = rolloutRuleRepository.save(rule);

        auditLogWriter.log(userId, flagEnv.getFlag().getProject().getOrganization().getId(),
                "ROLLOUT_RULE", saved.getId(), "UPDATE", "Set percentage rollout to " + request.getPercentage() + "%");

        return mapToRolloutRuleDto(saved);
    }

    @Transactional
    public void deleteFeatureFlag(UUID userId, UUID flagId) {
        FeatureFlag flag = featureFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("FeatureFlag", "id", flagId));

        auditLogWriter.log(userId, flag.getProject().getOrganization().getId(), "FEATURE_FLAG", flagId, "DELETE",
                "Deleted flag: " + flag.getKey());

        featureFlagRepository.delete(flag);
    }

    public FeatureFlagDto mapToDto(FeatureFlag flag) {
        List<FlagEnvironment> flagEnvs = flagEnvironmentRepository.findByFlagId(flag.getId());
        List<FlagEnvironmentDto> envDtos = flagEnvs.stream().map(this::mapToFlagEnvDto).collect(Collectors.toList());

        return FeatureFlagDto.builder()
                .id(flag.getId())
                .projectId(flag.getProject().getId())
                .key(flag.getKey())
                .name(flag.getName())
                .description(flag.getDescription())
                .flagType(flag.getFlagType())
                .defaultServeValue(flag.getDefaultServeValue())
                .enabled(flag.getEnabled())
                .environments(envDtos)
                .createdAt(flag.getCreatedAt())
                .updatedAt(flag.getUpdatedAt())
                .build();
    }

    public FlagEnvironmentDto mapToFlagEnvDto(FlagEnvironment fe) {
        List<TargetingRule> rules = targetingRuleRepository.findByFlagEnvironmentIdOrderByPriorityAsc(fe.getId());
        List<TargetingRuleDto> ruleDtos = rules.stream().map(this::mapToTargetingRuleDto).collect(Collectors.toList());

        Optional<RolloutRule> rolloutOpt = rolloutRuleRepository.findByFlagEnvironmentId(fe.getId());
        RolloutRuleDto rolloutDto = rolloutOpt.map(this::mapToRolloutRuleDto).orElse(null);

        return FlagEnvironmentDto.builder()
                .id(fe.getId())
                .flagId(fe.getFlag().getId())
                .environmentId(fe.getEnvironment().getId())
                .environmentName(fe.getEnvironment().getName())
                .environmentKey(fe.getEnvironment().getKey())
                .enabled(fe.getEnabled())
                .serveValue(fe.getServeValue())
                .targetingRules(ruleDtos)
                .rolloutRule(rolloutDto)
                .updatedAt(fe.getUpdatedAt())
                .build();
    }

    private TargetingRuleDto mapToTargetingRuleDto(TargetingRule tr) {
        List<String> values;
        try {
            values = objectMapper.readValue(tr.getValuesJson(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            values = List.of(tr.getValuesJson());
        }

        return TargetingRuleDto.builder()
                .id(tr.getId())
                .flagEnvironmentId(tr.getFlagEnvironment().getId())
                .priority(tr.getPriority())
                .attribute(tr.getAttribute())
                .operator(tr.getOperator())
                .values(values)
                .serveValue(tr.getServeValue())
                .build();
    }

    private RolloutRuleDto mapToRolloutRuleDto(RolloutRule rr) {
        return RolloutRuleDto.builder()
                .id(rr.getId())
                .flagEnvironmentId(rr.getFlagEnvironment().getId())
                .attribute(rr.getAttribute())
                .percentage(rr.getPercentage())
                .serveValueA(rr.getServeValueA())
                .serveValueB(rr.getServeValueB())
                .build();
    }
}
