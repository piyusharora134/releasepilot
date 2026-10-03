package com.releasepilot.feature;

import com.releasepilot.common.ApiResponse;
import com.releasepilot.common.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eval")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<EvaluationResultDto>> evaluate(
            @RequestHeader(value = "X-API-Key", required = false) String headerApiKey,
            @RequestParam(value = "apiKey", required = false) String paramApiKey,
            @Valid @RequestBody EvaluateRequest request) {

        String apiKey = StringUtils.hasText(headerApiKey) ? headerApiKey : paramApiKey;
        if (!StringUtils.hasText(apiKey)) {
            throw new UnauthorizedException("SDK API Key is required in 'X-API-Key' header or 'apiKey' query parameter");
        }

        EvaluationResultDto result = evaluationService.evaluateFlag(apiKey, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/evaluate-all")
    public ResponseEntity<ApiResponse<List<EvaluationResultDto>>> evaluateAll(
            @RequestHeader(value = "X-API-Key", required = false) String headerApiKey,
            @RequestParam(value = "apiKey", required = false) String paramApiKey,
            @RequestBody(required = false) Map<String, Object> context) {

        String apiKey = StringUtils.hasText(headerApiKey) ? headerApiKey : paramApiKey;
        if (!StringUtils.hasText(apiKey)) {
            throw new UnauthorizedException("SDK API Key is required in 'X-API-Key' header or 'apiKey' query parameter");
        }

        List<EvaluationResultDto> results = evaluationService.evaluateAllFlags(apiKey, context);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
