package com.releasepilot.feature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasepilot.audit.EvaluationLogRepository;
import com.releasepilot.common.HashUtils;
import com.releasepilot.project.Environment;
import com.releasepilot.project.EnvironmentRepository;
import com.releasepilot.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private FeatureFlagRepository featureFlagRepository;

    @Mock
    private FlagEnvironmentRepository flagEnvironmentRepository;

    @Mock
    private TargetingRuleRepository targetingRuleRepository;

    @Mock
    private RolloutRuleRepository rolloutRuleRepository;

    @Mock
    private EvaluationLogRepository evaluationLogRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private EvaluationService evaluationService;

    private String apiKey = "rp_sdk_dev_testkey123";
    private Environment environment;
    private Project project;
    private FeatureFlag featureFlag;
    private FlagEnvironment flagEnvironment;

    @BeforeEach
    void setUp() {
        project = Project.builder().id(UUID.randomUUID()).name("Test Project").key("test-project").build();
        environment = Environment.builder().id(UUID.randomUUID()).project(project).name("Development").key("dev").apiKey(apiKey).build();
        featureFlag = FeatureFlag.builder()
                .id(UUID.randomUUID())
                .project(project)
                .key("new-checkout-flow")
                .name("New Checkout Flow")
                .flagType(FlagType.BOOLEAN)
                .defaultServeValue("false")
                .enabled(true)
                .build();
        flagEnvironment = FlagEnvironment.builder()
                .id(UUID.randomUUID())
                .flag(featureFlag)
                .environment(environment)
                .enabled(true)
                .serveValue("false")
                .build();
    }

    @Test
    void testEvaluateFlag_TargetingRuleMatch() {
        when(environmentRepository.findByApiKey(apiKey)).thenReturn(Optional.of(environment));
        when(featureFlagRepository.findByProjectIdAndKey(project.getId(), "new-checkout-flow")).thenReturn(Optional.of(featureFlag));
        when(flagEnvironmentRepository.findByFlagIdAndEnvironmentId(featureFlag.getId(), environment.getId())).thenReturn(Optional.of(flagEnvironment));

        TargetingRule rule = TargetingRule.builder()
                .id(UUID.randomUUID())
                .flagEnvironment(flagEnvironment)
                .priority(0)
                .attribute("email")
                .operator(RuleOperator.ENDS_WITH)
                .valuesJson("[\"@company.com\"]")
                .serveValue("true")
                .build();

        when(targetingRuleRepository.findByFlagEnvironmentIdOrderByPriorityAsc(flagEnvironment.getId()))
                .thenReturn(List.of(rule));

        EvaluateRequest request = new EvaluateRequest("new-checkout-flow", Map.of("email", "john@company.com"));
        EvaluationResultDto result = evaluationService.evaluateFlag(apiKey, request);

        assertNotNull(result);
        assertEquals("new-checkout-flow", result.getFlagKey());
        assertEquals(true, result.getValue());
        assertEquals(EvaluationReason.TARGET_MATCH, result.getReason());
    }

    @Test
    void testEvaluateFlag_PercentageRollout() {
        when(environmentRepository.findByApiKey(apiKey)).thenReturn(Optional.of(environment));
        when(featureFlagRepository.findByProjectIdAndKey(project.getId(), "new-checkout-flow")).thenReturn(Optional.of(featureFlag));
        when(flagEnvironmentRepository.findByFlagIdAndEnvironmentId(featureFlag.getId(), environment.getId())).thenReturn(Optional.of(flagEnvironment));
        when(targetingRuleRepository.findByFlagEnvironmentIdOrderByPriorityAsc(flagEnvironment.getId())).thenReturn(Collections.emptyList());

        RolloutRule rolloutRule = RolloutRule.builder()
                .id(UUID.randomUUID())
                .flagEnvironment(flagEnvironment)
                .attribute("userId")
                .percentage(100) // 100% rollout
                .serveValueA("true")
                .serveValueB("false")
                .build();

        when(rolloutRuleRepository.findByFlagEnvironmentId(flagEnvironment.getId())).thenReturn(Optional.of(rolloutRule));

        EvaluateRequest request = new EvaluateRequest("new-checkout-flow", Map.of("userId", "user-123"));
        EvaluationResultDto result = evaluationService.evaluateFlag(apiKey, request);

        assertNotNull(result);
        assertEquals(true, result.getValue());
        assertEquals(EvaluationReason.PERCENTAGE_ROLLOUT, result.getReason());
    }

    @Test
    void testEvaluateFlag_DefaultValueWhenDisabled() {
        featureFlag.setEnabled(false);

        when(environmentRepository.findByApiKey(apiKey)).thenReturn(Optional.of(environment));
        when(featureFlagRepository.findByProjectIdAndKey(project.getId(), "new-checkout-flow")).thenReturn(Optional.of(featureFlag));

        EvaluateRequest request = new EvaluateRequest("new-checkout-flow", Collections.emptyMap());
        EvaluationResultDto result = evaluationService.evaluateFlag(apiKey, request);

        assertNotNull(result);
        assertEquals(false, result.getValue());
        assertEquals(EvaluationReason.FLAG_DISABLED, result.getReason());
    }
}
