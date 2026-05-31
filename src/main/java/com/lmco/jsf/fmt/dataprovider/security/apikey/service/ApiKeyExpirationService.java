package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class ApiKeyExpirationService {

    @Inject
    private ApiKeyRepository keyRepository;

    @Transactional
    public void expireEligibleKeys() {
        for (ApiKey apiKey : keyRepository.listExpiredActiveKeys(0, 500)) {
            expireIfExpired(apiKey);
        }
        for (ApiKey apiKey : keyRepository.listRotationGraceEndedActiveKeys(0, 500)) {
            rotateIfGraceEnded(apiKey);
        }
    }

    @Transactional
    public boolean expireIfExpired(ApiKey apiKey) {
        if (apiKey == null || apiKey.getExpiresAt() == null || apiKey.getExpiresAt().isAfter(Instant.now())) {
            return false;
        }
        if (apiKey.getStatus() != ApiKeyStatus.EXPIRED) {
            apiKey.setStatus(ApiKeyStatus.EXPIRED);
            keyRepository.update(apiKey);
        }
        return true;
    }

    @Transactional
    public boolean rotateIfGraceEnded(ApiKey apiKey) {
        if (apiKey == null
                || apiKey.getRotationInitiatedAt() == null
                || apiKey.getGracePeriodEndsAt() == null
                || apiKey.getGracePeriodEndsAt().isAfter(Instant.now())) {
            return false;
        }
        if (apiKey.getStatus() != ApiKeyStatus.ROTATED) {
            apiKey.setStatus(ApiKeyStatus.ROTATED);
            if (apiKey.getRotatedAt() == null) {
                apiKey.setRotatedAt(Instant.now());
            }
            keyRepository.update(apiKey);
        }
        return true;
    }
}
