package com.releasepilot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "releasepilot")
public class ReleasePilotProperties {

    private Cache cache = new Cache();

    @Data
    public static class Cache {
        private boolean enabled = false;
        private int ttlSeconds = 30;
    }
}
