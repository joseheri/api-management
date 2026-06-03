package com.lmco.jsf.fmt.dataprovider.security.apikey.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ApiKeySecurityConfig {

    public static final String DEFAULT_API_KEY_HEADER = "X-API-Key";
    public static final String DEFAULT_DEV_ADMIN_HEADER = "X-Dev-Admin-User";

    @ConfigProperty(name = "apikey.secret")
    private String hmacSecret;

    @ConfigProperty(name = "apikey.header-name", defaultValue = DEFAULT_API_KEY_HEADER)
    private String apiKeyHeaderName;

    @ConfigProperty(name = "apikey.enforcement.mode", defaultValue = "enforce")
    private String enforcementMode;

    @ConfigProperty(name = "apikey.dev-admin.enabled", defaultValue = "false")
    private boolean devAdminEnabled;

    @ConfigProperty(name = "apikey.dev-admin.header-name", defaultValue = DEFAULT_DEV_ADMIN_HEADER)
    private String devAdminHeaderName;

    public String getHmacSecret() {
        return hmacSecret;
    }

    public String getApiKeyHeaderName() {
        return apiKeyHeaderName;
    }

    public ApiKeyEnforcementMode getEnforcementMode() {
        return ApiKeyEnforcementMode.fromConfig(enforcementMode);
    }

    public boolean isEnforcementEnabled() {
        return getEnforcementMode() == ApiKeyEnforcementMode.ENFORCE;
    }

    public boolean isReportOnly() {
        return getEnforcementMode() == ApiKeyEnforcementMode.REPORT_ONLY;
    }

    public boolean isEnforcementDisabled() {
        return getEnforcementMode() == ApiKeyEnforcementMode.DISABLED;
    }

    public boolean isDevAdminEnabled() {
        return devAdminEnabled;
    }

    public String getDevAdminHeaderName() {
        return devAdminHeaderName;
    }
}
