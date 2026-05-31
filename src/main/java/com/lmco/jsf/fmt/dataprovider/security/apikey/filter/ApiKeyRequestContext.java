package com.lmco.jsf.fmt.dataprovider.security.apikey.filter;

import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyValidator.AuthenticatedApiKey;
import jakarta.enterprise.context.RequestScoped;
import java.util.Optional;

@RequestScoped
public class ApiKeyRequestContext {

    private AuthenticatedApiKey authenticatedApiKey;
    private String devAdminUser;

    public Optional<AuthenticatedApiKey> getAuthenticatedApiKey() {
        return Optional.ofNullable(authenticatedApiKey);
    }

    public void setAuthenticatedApiKey(AuthenticatedApiKey authenticatedApiKey) {
        this.authenticatedApiKey = authenticatedApiKey;
    }

    public Optional<String> getDevAdminUser() {
        return Optional.ofNullable(devAdminUser);
    }

    public void setDevAdminUser(String devAdminUser) {
        this.devAdminUser = devAdminUser;
    }

    public void clear() {
        authenticatedApiKey = null;
        devAdminUser = null;
    }
}
