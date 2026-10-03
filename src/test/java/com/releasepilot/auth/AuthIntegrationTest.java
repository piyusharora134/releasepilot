package com.releasepilot.auth;

import com.releasepilot.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerLoginAndFetchProfile() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Integration User");
        registerRequest.setEmail("integration@test.local");
        registerRequest.setPassword("secret123");

        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                "/api/v1/auth/register", registerRequest, Map.class);

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResponse.getBody()).isNotNull();
        assertThat(registerResponse.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> registerData = (Map<String, Object>) registerResponse.getBody().get("data");
        String token = (String) registerData.get("accessToken");
        assertThat(token).isNotBlank();

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("integration@test.local");
        loginRequest.setPassword("secret123");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, Map.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().get("success")).isEqualTo(true);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Map> meResponse = restTemplate.exchange(
                "/api/v1/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) meResponse.getBody().get("data");
        assertThat(user.get("email")).isEqualTo("integration@test.local");
    }
}
