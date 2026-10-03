package com.releasepilot.feature;

import com.releasepilot.AbstractIntegrationTest;
import com.releasepilot.auth.RegisterRequest;
import com.releasepilot.project.CreateProjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void evaluateBooleanFlagViaSdkApiKey() {
        String token = registerAndGetToken();

        UUID organizationId = fetchFirstOrganizationId(token);
        UUID projectId = createProject(token, organizationId);
        String apiKey = fetchDevEnvironmentApiKey(token, projectId);
        createBooleanFlag(token, projectId, "dark-mode", "false");

        HttpHeaders evalHeaders = new HttpHeaders();
        evalHeaders.set("X-API-Key", apiKey);

        Map<String, Object> evalBody = Map.of(
                "flagKey", "dark-mode",
                "context", Map.of("userId", "user-123")
        );

        ResponseEntity<Map> evalResponse = restTemplate.postForEntity(
                "/api/v1/eval/evaluate",
                new HttpEntity<>(evalBody, evalHeaders),
                Map.class);

        assertThat(evalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(evalResponse.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) evalResponse.getBody().get("data");
        assertThat(result.get("flagKey")).isEqualTo("dark-mode");
        assertThat(result.get("value")).isIn(true, false, "true", "false");
    }

    private String registerAndGetToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Eval User");
        request.setEmail("eval@test.local");
        request.setPassword("secret123");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/v1/auth/register", request, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("accessToken");
    }

    private UUID fetchFirstOrganizationId(String token) {
        HttpHeaders headers = authHeaders(token);
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/organizations",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orgs = (List<Map<String, Object>>) response.getBody().get("data");
        return UUID.fromString((String) orgs.get(0).get("id"));
    }

    private UUID createProject(String token, UUID organizationId) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setOrganizationId(organizationId);
        request.setName("Web App");
        request.setKey("web-app");
        request.setDescription("Integration test project");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/projects",
                new HttpEntity<>(request, authHeaders(token)),
                Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) response.getBody().get("data");
        return UUID.fromString((String) project.get("id"));
    }

    private String fetchDevEnvironmentApiKey(String token, UUID projectId) {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/projects/" + projectId,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> envs = (List<Map<String, Object>>) ((Map<String, Object>) response.getBody().get("data")).get("environments");

        return envs.stream()
                .filter(env -> "dev".equals(env.get("key")))
                .map(env -> (String) env.get("apiKey"))
                .findFirst()
                .orElseThrow();
    }

    private void createBooleanFlag(String token, UUID projectId, String key, String defaultValue) {
        CreateFeatureFlagRequest request = new CreateFeatureFlagRequest();
        request.setProjectId(projectId);
        request.setKey(key);
        request.setName("Dark Mode");
        request.setFlagType(FlagType.BOOLEAN);
        request.setDefaultServeValue(defaultValue);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/flags",
                new HttpEntity<>(request, authHeaders(token)),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
