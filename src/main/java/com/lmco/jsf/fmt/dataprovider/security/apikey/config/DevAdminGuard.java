package com.lmco.jsf.fmt.dataprovider.security.apikey.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ProfileManager;
import java.util.Optional;

@ApplicationScoped
public class DevAdminGuard {

    public static final String DEV_ADMIN_HEADER = ApiKeySecurityConfig.DEFAULT_DEV_ADMIN_HEADER;

    @Inject
    private ApiKeySecurityConfig securityConfig;

    public Optional<String> getDevAdminUser(HttpHeaders headers) {
        if (!isDevAdminAllowed() || headers == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(headers.getHeaderString(securityConfig.getDevAdminHeaderName()))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    public Optional<String> getDevAdminUser(ContainerRequestContext requestContext) {
        if (!isDevAdminAllowed() || requestContext == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(requestContext.getHeaderString(securityConfig.getDevAdminHeaderName()))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    private boolean isDevAdminAllowed() {
        return securityConfig.isDevAdminEnabled() && isDevOrTestRuntime();
    }

    private boolean isDevOrTestRuntime() {
        LaunchMode launchMode = LaunchMode.current();
        if (launchMode == LaunchMode.DEVELOPMENT || launchMode == LaunchMode.TEST) {
            return true;
        }

        String activeProfile = ProfileManager.getActiveProfile();
        return "dev".equals(activeProfile) || "test".equals(activeProfile);
    }
}
