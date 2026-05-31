package com.lmco.jsf.fmt.dataprovider.security.apikey.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Optional;

@ApplicationScoped
public class DevAdminGuard {

    public static final String DEV_ADMIN_HEADER = ApiKeySecurityConfig.DEFAULT_DEV_ADMIN_HEADER;

    @Inject
    private ApiKeySecurityConfig securityConfig;

    public Optional<String> getDevAdminUser(HttpHeaders headers) {
        if (!securityConfig.isDevAdminEnabled() || headers == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(headers.getHeaderString(securityConfig.getDevAdminHeaderName()))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }
}
