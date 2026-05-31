package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateClaimInvitationRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RotateApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyScopeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class ApiKeyRotationService {

    private static final String SYSTEM_ACTOR = "system";

    @Inject
    private ApiKeyRepository keyRepository;

    @Inject
    private ApiKeyScopeRepository scopeRepository;

    @Inject
    private ApiKeyClaimService claimService;

    @Inject
    private ApiKeyAuditService auditService;

    @Transactional
    public ClaimInvitationCreatedResponse rotateKey(Long keyId, RotateApiKeyRequest request) {
        if (keyId == null) {
            throw new BadRequestException("keyId is required");
        }
        ApiKey existingKey = keyRepository.findById(keyId)
                .orElseThrow(() -> new NotFoundException("API key not found"));
        if (existingKey.getStatus() == ApiKeyStatus.REVOKED) {
            throw new ForbiddenException("Revoked API keys cannot be rotated");
        }
        if (existingKey.getStatus() == ApiKeyStatus.EXPIRED) {
            throw new WebApplicationException("Expired API keys cannot be rotated", Response.Status.GONE);
        }
        if (existingKey.getStatus() == ApiKeyStatus.ROTATED) {
            throw new ForbiddenException("Rotated API keys cannot be rotated again");
        }

        Instant replacementExpiresAt = request == null ? null : request.getReplacementExpiresAt();
        if (replacementExpiresAt == null || !replacementExpiresAt.isAfter(Instant.now())) {
            throw new BadRequestException("replacementExpiresAt must be in the future");
        }

        CreateClaimInvitationRequest invitationRequest = new CreateClaimInvitationRequest();
        invitationRequest.setApprovedEmail(existingKey.getClient().getContactEmail());
        invitationRequest.setKeyName(existingKey.getKeyName() + " rotation");
        invitationRequest.setEnvironment(existingKey.getEnvironment());
        invitationRequest.setScopes(scopeRepository.listScopeNamesByApiKeyId(existingKey.getId()));
        invitationRequest.setExpiresAt(replacementExpiresAt);
        invitationRequest.setApprovalReference("rotation for key " + existingKey.getId());

        ClaimInvitationCreatedResponse response = claimService.createInvitation(existingKey.getClient().getId(),
                invitationRequest);
        if (existingKey.getRotationGroupId() == null || existingKey.getRotationGroupId().isBlank()) {
            existingKey.setRotationGroupId(UUID.randomUUID().toString());
        }
        existingKey.setRotationInitiatedAt(Instant.now());
        existingKey.setGracePeriodEndsAt(replacementExpiresAt);
        keyRepository.update(existingKey);
        auditService.recordEvent(existingKey.getClient().getId(), existingKey.getId(),
                ApiKeyAuditEventType.KEY_ROTATED, SYSTEM_ACTOR, "reason=" + safeReason(request));
        return response;
    }

    private String safeReason(RotateApiKeyRequest request) {
        if (request == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            return "not provided";
        }
        return request.getReason().trim();
    }
}
