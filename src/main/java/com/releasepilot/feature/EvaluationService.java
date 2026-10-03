package com.releasepilot.feature;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.releasepilot.audit.EvaluationLog;
import com.releasepilot.audit.EvaluationLogRepository;
import com.releasepilot.common.HashUtils;
import com.releasepilot.common.ResourceNotFoundException;
import com.releasepilot.project.Environment;
import com.releasepilot.project.EnvironmentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EnvironmentRepository environmentRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final FlagEnvironmentRepository flagEnvironmentRepository;
    private final TargetingRuleRepository targetingRuleRepository;
    private final RolloutRuleRepository rolloutRuleRepository;
    private final EvaluationLogRepository evaluationLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private CacheManager cacheManager;

    private static final String EVALUATIONS_CACHE = "evaluations";

    @Transactional
    public EvaluationResultDto evaluateFlag(String apiKey, EvaluateRequest request) {
        Environment environment = environmentRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid SDK API Key"));

        FeatureFlag flag = featureFlagRepository.findByProjectIdAndKey(environment.getProject().getId(), request.getFlagKey())
                .orElseThrow(() -> new ResourceNotFoundException("FeatureFlag", "key", request.getFlagKey()));

        Map<String, Object> context = request.getContext() != null ? request.getContext() : Collections.emptyMap();
        String cacheKey = buildCacheKey(environment.getId(), flag.getKey(), context);

        EvaluationResultDto cached = getCachedResult(cacheKey);
        if (cached != null) {
            return cached;
        }

        EvaluationResultDto result = performEvaluation(flag, environment, context);
        putCachedResult(cacheKey, result);

        // Async/background logging of evaluation
        try {
            String userContextId = context.containsKey("userId") ? String.valueOf(context.get("userId"))
                    : (context.containsKey("email") ? String.valueOf(context.get("email")) : "anonymous");

            EvaluationLog log = EvaluationLog.builder()
                    .environmentId(environment.getId())
                    .flagKey(flag.getKey())
                    .userContextId(userContextId)
                    .userContextJson(objectMapper.writeValueAsString(context))
                    .resultValue(String.valueOf(result.getValue()))
                    .reason(result.getReason().name())
                    .evaluatedAt(Instant.now())
                    .build();

            evaluationLogRepository.save(log);
        } catch (Exception ignored) {}

        return result;
    }

    @Transactional
    public List<EvaluationResultDto> evaluateAllFlags(String apiKey, Map<String, Object> context) {
        Environment environment = environmentRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid SDK API Key"));

        List<FeatureFlag> flags = featureFlagRepository.findByProjectId(environment.getProject().getId());
        List<EvaluationResultDto> results = new ArrayList<>();

        for (FeatureFlag flag : flags) {
            results.add(performEvaluation(flag, environment, context != null ? context : Collections.emptyMap()));
        }

        return results;
    }

    private EvaluationResultDto performEvaluation(FeatureFlag flag, Environment environment, Map<String, Object> context) {
        // 1. Check globally enabled
        if (!Boolean.TRUE.equals(flag.getEnabled())) {
            return buildResult(flag, flag.getDefaultServeValue(), EvaluationReason.FLAG_DISABLED, environment.getKey());
        }

        Optional<FlagEnvironment> flagEnvOpt = flagEnvironmentRepository.findByFlagIdAndEnvironmentId(flag.getId(), environment.getId());

        if (flagEnvOpt.isEmpty() || !Boolean.TRUE.equals(flagEnvOpt.get().getEnabled())) {
            return buildResult(flag, flag.getDefaultServeValue(), EvaluationReason.ENVIRONMENT_DISABLED, environment.getKey());
        }

        FlagEnvironment flagEnv = flagEnvOpt.get();

        // 2. Evaluate Targeting Rules
        List<TargetingRule> targetingRules = targetingRuleRepository.findByFlagEnvironmentIdOrderByPriorityAsc(flagEnv.getId());
        for (TargetingRule rule : targetingRules) {
            if (evaluateTargetingRule(rule, context)) {
                return buildResult(flag, rule.getServeValue(), EvaluationReason.TARGET_MATCH, environment.getKey());
            }
        }

        // 3. Evaluate Percentage Rollout Rule
        Optional<RolloutRule> rolloutOpt = rolloutRuleRepository.findByFlagEnvironmentId(flagEnv.getId());
        if (rolloutOpt.isPresent()) {
            RolloutRule rollout = rolloutOpt.get();
            String attrKey = rollout.getAttribute() != null ? rollout.getAttribute() : "userId";
            String attrValue = context.containsKey(attrKey) ? String.valueOf(context.get(attrKey)) : UUID.randomUUID().toString();

            int bucket = HashUtils.getBucket(flag.getKey() + ":" + attrValue);
            if (bucket < rollout.getPercentage()) {
                return buildResult(flag, rollout.getServeValueA(), EvaluationReason.PERCENTAGE_ROLLOUT, environment.getKey());
            } else {
                return buildResult(flag, rollout.getServeValueB(), EvaluationReason.PERCENTAGE_ROLLOUT, environment.getKey());
            }
        }

        // 4. Default Serve Value for Environment
        String finalValue = flagEnv.getServeValue() != null ? flagEnv.getServeValue() : flag.getDefaultServeValue();
        return buildResult(flag, finalValue, EvaluationReason.DEFAULT_VALUE, environment.getKey());
    }

    private boolean evaluateTargetingRule(TargetingRule rule, Map<String, Object> context) {
        if (!context.containsKey(rule.getAttribute())) {
            return false;
        }

        String contextValue = String.valueOf(context.get(rule.getAttribute()));
        List<String> targetValues = parseJsonValues(rule.getValuesJson());

        switch (rule.getOperator()) {
            case EQUALS:
                return targetValues.stream().anyMatch(v -> v.equalsIgnoreCase(contextValue));
            case NOT_EQUALS:
                return targetValues.stream().noneMatch(v -> v.equalsIgnoreCase(contextValue));
            case CONTAINS:
                return targetValues.stream().anyMatch(contextValue::contains);
            case IN:
                return targetValues.contains(contextValue);
            case NOT_IN:
                return !targetValues.contains(contextValue);
            case STARTS_WITH:
                return targetValues.stream().anyMatch(contextValue::startsWith);
            case ENDS_WITH:
                return targetValues.stream().anyMatch(contextValue::endsWith);
            default:
                return false;
        }
    }

    private List<String> parseJsonValues(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private EvaluationResultDto buildResult(FeatureFlag flag, String rawValue, EvaluationReason reason, String envKey) {
        Object parsedValue = parseTypedValue(flag.getFlagType(), rawValue);
        return EvaluationResultDto.builder()
                .flagKey(flag.getKey())
                .value(parsedValue)
                .reason(reason)
                .environmentKey(envKey)
                .build();
    }

    private Object parseTypedValue(FlagType flagType, String rawValue) {
        if (rawValue == null) return null;
        try {
            switch (flagType) {
                case BOOLEAN:
                    return Boolean.parseBoolean(rawValue);
                case NUMERIC:
                    if (rawValue.contains(".")) {
                        return Double.parseDouble(rawValue);
                    }
                    return Long.parseLong(rawValue);
                case JSON:
                    return objectMapper.readValue(rawValue, Object.class);
                case STRING:
                default:
                    return rawValue;
            }
        } catch (Exception e) {
            return rawValue;
        }
    }

    private EvaluationResultDto getCachedResult(String cacheKey) {
        if (cacheManager == null) {
            return null;
        }
        Cache cache = cacheManager.getCache(EVALUATIONS_CACHE);
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(cacheKey);
        return wrapper != null ? (EvaluationResultDto) wrapper.get() : null;
    }

    private void putCachedResult(String cacheKey, EvaluationResultDto result) {
        if (cacheManager == null) {
            return;
        }
        Cache cache = cacheManager.getCache(EVALUATIONS_CACHE);
        if (cache != null) {
            cache.put(cacheKey, result);
        }
    }

    private String buildCacheKey(UUID environmentId, String flagKey, Map<String, Object> context) {
        try {
            String payload = environmentId + ":" + flagKey + ":" + objectMapper.writeValueAsString(new TreeMap<>(context));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return environmentId + ":" + flagKey;
        }
    }
}
