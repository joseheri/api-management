package com.lmco.jsf.fmt.dataprovider.security.apikey.config;

import java.util.Locale;

public enum ApiKeyEnforcementMode {
    ENFORCE,
    REPORT_ONLY,
    DISABLED;

    public static ApiKeyEnforcementMode fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return ENFORCE;
        }

        String normalized = value.trim()
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
        for (ApiKeyEnforcementMode mode : values()) {
            if (mode.name().equals(normalized)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Unsupported apikey.enforcement.mode: " + value);
    }
}
