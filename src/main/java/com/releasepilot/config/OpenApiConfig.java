package com.releasepilot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ReleasePilot API",
                version = "1.0",
                description = "Feature flag and progressive rollout platform REST API",
                contact = @Contact(name = "ReleasePilot")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@SecurityScheme(
        name = "apiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        paramName = "X-API-Key",
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
