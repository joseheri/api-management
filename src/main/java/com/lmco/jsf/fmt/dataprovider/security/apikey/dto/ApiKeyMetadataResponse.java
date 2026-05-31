package com.lmco.jsf.fmt.dataprovider.security.apikey.dto;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import java.time.Instant;
import java.util.List;

public class ApiKeyMetadataResponse {

    private Long id;
    private Long clientId;
    private String keyName;
    private String keyPrefix;
    private Environment environment;
    private ApiKeyStatus status;
    private List<String> scopes;
    private Instant createdAt;
    private Instant expiresAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ApiKeyStatus getStatus() {
        return status;
    }

    public void setStatus(ApiKeyStatus status) {
        this.status = status;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
