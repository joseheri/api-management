package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiKeyMetadataResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RevokeApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RotateApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiClientRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyScopeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class ApiKeyService {

    private static final int DEFAULT_LIST_LIMIT = 500;
    private static final String SYSTEM_ACTOR = "system";

    @Inject
    private ApiClientRepository clientRepository;

    @Inject
    private ApiKeyRepository keyRepository;

    @Inject
    private ApiKeyScopeRepository scopeRepository;

    @Inject
    private ApiKeyRotationService rotationService;

    @Inject
    private ApiKeyAuditService auditService;

    public List<ApiKeyMetadataResponse> listKeysForClient(Long clientId) {
        if (clientId == null) {
            throw new BadRequestException("clientId is required");
        }
        clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("API client not found"));
        return keyRepository.listByClientId(clientId, 0, DEFAULT_LIST_LIMIT)
                .stream()
                .map(this::toMetadataResponse)
                .toList();
    }

    public ApiKeyMetadataResponse getKey(Long keyId) {
        return toMetadataResponse(findKey(keyId));
    }

    @Transactional
    public ApiKeyMetadataResponse revokeKey(Long keyId, RevokeApiKeyRequest request) {
        ApiKey apiKey = findKey(keyId);
        if (apiKey.getStatus() != ApiKeyStatus.REVOKED) {
            apiKey.setStatus(ApiKeyStatus.REVOKED);
            apiKey.setRevokedAt(Instant.now());
            apiKey.setRevokedBy(SYSTEM_ACTOR);
            apiKey.setRevocationReason(safeReason(request == null ? null : request.getReason()));
            keyRepository.update(apiKey);
            auditService.recordEvent(apiKey.getClient().getId(), apiKey.getId(), ApiKeyAuditEventType.KEY_REVOKED,
                    SYSTEM_ACTOR, "reason=" + safeReason(request == null ? null : request.getReason()));
        }
        return toMetadataResponse(apiKey);
    }

    public ClaimInvitationCreatedResponse rotateKey(Long keyId, RotateApiKeyRequest request) {
        return rotationService.rotateKey(keyId, request);
    }

    private ApiKey findKey(Long keyId) {
        if (keyId == null) {
            throw new BadRequestException("keyId is required");
        }
        return keyRepository.findById(keyId)
                .orElseThrow(() -> new NotFoundException("API key not found"));
    }

    private ApiKeyMetadataResponse toMetadataResponse(ApiKey apiKey) {
        ApiKeyMetadataResponse response = new ApiKeyMetadataResponse();
        response.setId(apiKey.getId());
        response.setClientId(apiKey.getClient().getId());
        response.setKeyName(apiKey.getKeyName());
        response.setKeyPrefix(apiKey.getKeyPrefix());
        response.setEnvironment(apiKey.getEnvironment());
        response.setStatus(apiKey.getStatus());
        response.setScopes(scopeRepository.listScopeNamesByApiKeyId(apiKey.getId()));
        response.setCreatedAt(apiKey.getCreatedAt());
        response.setExpiresAt(apiKey.getExpiresAt());
        response.setRevokedAt(apiKey.getRevokedAt());
        return response;
    }

    private String safeReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return "not provided";
        }
        return reason.trim();
    }
}
