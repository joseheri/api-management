package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClientStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyScopeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyValidator {

    @Inject
    private ApiKeyRepository keyRepository;

    @Inject
    private ApiKeyScopeRepository scopeRepository;

    @Inject
    private ApiKeyHasher keyHasher;

    @Inject
    private ApiKeyAuditService auditService;

    public boolean isValid(String rawApiKey) {
        return authenticate(rawApiKey).isPresent();
    }

    @Transactional
    public Optional<AuthenticatedApiKey> authenticate(String rawApiKey) {
        String keyPrefix = extractKeyPrefix(rawApiKey);
        if (keyPrefix == null) {
            recordAuthenticationFailure(null, null, "malformed API key");
            return Optional.empty();
        }

        Optional<ApiKey> apiKeyOptional = keyRepository.findByKeyPrefix(keyPrefix);
        if (apiKeyOptional.isEmpty()) {
            recordAuthenticationFailure(null, null, "unknown key prefix");
            return Optional.empty();
        }

        ApiKey apiKey = apiKeyOptional.get();
        if (apiKey.getStatus() == ApiKeyStatus.REVOKED) {
            recordAuthenticationFailure(apiKey.getClient().getId(), apiKey.getId(), "revoked API key");
            return Optional.empty();
        }
        if (apiKey.getStatus() == ApiKeyStatus.ROTATED) {
            recordAuthenticationFailure(apiKey.getClient().getId(), apiKey.getId(), "rotated API key");
            return Optional.empty();
        }
        if (apiKey.getStatus() == ApiKeyStatus.EXPIRED || isExpired(apiKey)) {
            apiKey.setStatus(ApiKeyStatus.EXPIRED);
            keyRepository.update(apiKey);
            recordAuthenticationFailure(apiKey.getClient().getId(), apiKey.getId(), "expired API key");
            return Optional.empty();
        }
        if (isRotatedPastGrace(apiKey)) {
            apiKey.setStatus(ApiKeyStatus.ROTATED);
            if (apiKey.getRotatedAt() == null) {
                apiKey.setRotatedAt(Instant.now());
            }
            keyRepository.update(apiKey);
            recordAuthenticationFailure(apiKey.getClient().getId(), apiKey.getId(), "rotated API key");
            return Optional.empty();
        }
        if (apiKey.getStatus() != ApiKeyStatus.ACTIVE) {
            recordAuthenticationFailure(apiKey.getClient().getId(), apiKey.getId(), "inactive API key");
            return Optional.empty();
        }
        if (apiKey.getClient().getStatus() != ApiClientStatus.ACTIVE) {
            recordAuthenticationFailure(apiKey.getClient().getId(), apiKey.getId(), "disabled API client");
            return Optional.empty();
        }
        if (!keyHasher.verify(rawApiKey, apiKey.getKeyHash())) {
            recordAuthenticationFailure(apiKey.getClient().getId(), apiKey.getId(), "API key hash mismatch");
            return Optional.empty();
        }

        apiKey.setLastUsedAt(Instant.now());
        keyRepository.update(apiKey);
        List<String> scopes = scopeRepository.listScopeNamesByApiKeyId(apiKey.getId());
        return Optional.of(new AuthenticatedApiKey(
                apiKey.getId(),
                apiKey.getClient().getId(),
                apiKey.getClient().getClientName(),
                apiKey.getKeyPrefix(),
                apiKey.getEnvironment(),
                scopes));
    }

    public String extractKeyPrefix(String rawApiKey) {
        if (rawApiKey == null || rawApiKey.isBlank()) {
            return null;
        }
        int separator = rawApiKey.indexOf('.');
        if (separator <= 0 || separator == rawApiKey.length() - 1) {
            return null;
        }
        String keyPrefix = rawApiKey.substring(0, separator);
        if (!keyPrefix.matches("^ak_[a-z]+_[A-Z0-9]{8}$")) {
            return null;
        }
        return keyPrefix;
    }

    private boolean isExpired(ApiKey apiKey) {
        return apiKey.getExpiresAt() != null && !apiKey.getExpiresAt().isAfter(Instant.now());
    }

    private boolean isRotatedPastGrace(ApiKey apiKey) {
        return apiKey.getRotationInitiatedAt() != null
                && apiKey.getGracePeriodEndsAt() != null
                && !apiKey.getGracePeriodEndsAt().isAfter(Instant.now());
    }

    private void recordAuthenticationFailure(Long clientId, Long keyId, String reason) {
        auditService.recordEvent(clientId, keyId, ApiKeyAuditEventType.AUTHENTICATION_FAILED, "api-key-validator",
                "reason=" + reason);
    }

    public record AuthenticatedApiKey(
            Long keyId,
            Long clientId,
            String clientName,
            String keyPrefix,
            Environment environment,
            List<String> scopes) {
    }
}
