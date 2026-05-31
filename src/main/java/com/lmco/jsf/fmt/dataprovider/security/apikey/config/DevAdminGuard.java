package com.lmco.jsf.fmt.dataprovider.security.apikey.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Optional;

@ApplicationScoped
public class DevAdminGuard {

    public static final String DEV_ADMIN_HEADER = "X-Dev-Admin-User";

    public Optional<String> getDevAdminUser(HttpHeaders headers) {
        if (headers == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(headers.getHeaderString(DEV_ADMIN_HEADER))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }
}
