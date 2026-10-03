package com.releasepilot.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtils {

    /**
     * Hashes a context key (e.g., flagKey + ":" + userId) to a deterministic integer in range [0, 99].
     */
    public static int getBucket(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Take first 4 bytes to form a 32-bit integer
            int hash = ((digest[0] & 0xFF) << 24) |
                       ((digest[1] & 0xFF) << 16) |
                       ((digest[2] & 0xFF) << 8)  |
                       (digest[3] & 0xFF);

            return Math.abs(hash % 100);
        } catch (Exception e) {
            return Math.abs(input.hashCode() % 100);
        }
    }
}
