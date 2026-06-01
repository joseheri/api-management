package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimReviewResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateClaimInvitationRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClient;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClientStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyClaimInvitation;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyScope;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ClaimInvitationStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiClientRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyClaimInvitationRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyScopeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ApiKeyClaimService {

    private static final int MAX_KEY_GENERATION_ATTEMPTS = 5;
    private static final int DEFAULT_MAX_CLAIM_ATTEMPTS = 5;
    private static final String CLAIM_ACTOR = "claim";
    private static final String SYSTEM_ACTOR = "system";
    private static final Set<String> ALLOWED_SCOPES = Set.of(
            "read:aircraft",
            "read:maintenance",
            "read:pairs",
            "read:usage");

    @Inject
    private ApiClientRepository clientRepository;

    @Inject
    private ApiKeyClaimInvitationRepository invitationRepository;

    @Inject
    private ApiKeyRepository keyRepository;

    @Inject
    private ApiKeyScopeRepository scopeRepository;

    @Inject
    private ApiKeyGenerator keyGenerator;

    @Inject
    private ApiKeyHasher keyHasher;

    @Inject
    private ClaimCodeHasher claimCodeHasher;

    @Inject
    private ApiKeyAuditService auditService;

    @Transactional
    public ClaimInvitationCreatedResponse createInvitation(Long clientId, CreateClaimInvitationRequest request) {
        requireRequest(request);
        ApiClient client = findClient(clientId);
        if (client.getStatus() != ApiClientStatus.ACTIVE) {
            throw new ForbiddenException("API client is disabled");
        }

        String approvedEmail = requireText(request.getApprovedEmail(), "approvedEmail").toLowerCase();
        String keyName = requireText(request.getKeyName(), "keyName");
        if (request.getEnvironment() == null) {
            throw new BadRequestException("environment is required");
        }
        Instant now = Instant.now();
        if (request.getExpiresAt() == null || !request.getExpiresAt().isAfter(now)) {
            throw new BadRequestException("expiresAt must be in the future");
        }
        List<String> scopes = normalizeScopes(request.getScopes());

        invitationRepository.findByApprovedEmailAndStatus(approvedEmail, ClaimInvitationStatus.PENDING)
                .ifPresent(existing -> {
                    if (isExpired(existing, now)) {
                        existing.setStatus(ClaimInvitationStatus.EXPIRED);
                        invitationRepository.update(existing);
                    } else {
                        throw new WebApplicationException(
                                "A pending claim invitation already exists for the approved email.",
                                Response.Status.CONFLICT);
                    }
                });

        String rawClaimCode = keyGenerator.generateClaimCode();
        ApiKeyClaimInvitation invitation = new ApiKeyClaimInvitation();
        invitation.setClient(client);
        invitation.setApprovedEmail(approvedEmail);
        invitation.setClaimCodeHash(claimCodeHasher.hash(rawClaimCode));
        invitation.setKeyName(keyName);
        invitation.setEnvironment(request.getEnvironment());
        invitation.setStatus(ClaimInvitationStatus.PENDING);
        invitation.setApprovalReference(trimToNull(request.getApprovalReference()));
        invitation.setCreatedAt(now);
        invitation.setExpiresAt(request.getExpiresAt());
        invitation.setScopes(scopes);
        invitation.setFailedAttemptCount(0);
        invitation.setMaxAttempts(DEFAULT_MAX_CLAIM_ATTEMPTS);
        invitationRepository.persist(invitation);

        auditService.recordEvent(client.getId(), null, ApiKeyAuditEventType.INVITATION_CREATED, SYSTEM_ACTOR,
                "invitationId=" + invitation.getId() + "; approvedEmail=" + approvedEmail);
        return toInvitationResponse(invitation, invitation.getScopes(), rawClaimCode);
    }

    @Transactional(dontRollbackOn = WebApplicationException.class)
    public ClaimReviewResponse reviewClaim(ClaimKeyRequest request) {
        ApiKeyClaimInvitation invitation = validateClaimRequest(request);
        return toClaimReviewResponse(invitation, List.copyOf(invitation.getScopes()));
    }

    @Transactional(dontRollbackOn = WebApplicationException.class)
    public ClaimKeyResponse claimKey(ClaimKeyRequest request) {
        ApiKeyClaimInvitation invitation = validateClaimRequest(request);
        ApiClient client = invitation.getClient();
        if (client.getStatus() != ApiClientStatus.ACTIVE) {
            throw new ForbiddenException("API client is disabled");
        }

        ApiKeyGenerator.GeneratedApiKey generatedKey = generateUniqueApiKey(invitation);
        ApiKey apiKey = new ApiKey();
        apiKey.setClient(client);
        apiKey.setKeyPrefix(generatedKey.keyPrefix());
        apiKey.setKeyHash(keyHasher.hmacSha256(generatedKey.apiKey()));
        apiKey.setKeyName(invitation.getKeyName());
        apiKey.setEnvironment(invitation.getEnvironment());
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setCreatedAt(Instant.now());
        apiKey.setExpiresAt(invitation.getExpiresAt());
        keyRepository.persist(apiKey);

        List<String> scopes = List.copyOf(invitation.getScopes());
        persistScopes(apiKey, scopes);

        invitation.setStatus(ClaimInvitationStatus.CLAIMED);
        invitation.setClaimedAt(Instant.now());
        invitationRepository.update(invitation);

        auditService.recordEvent(client.getId(), apiKey.getId(), ApiKeyAuditEventType.KEY_CLAIMED, CLAIM_ACTOR,
                "invitationId=" + invitation.getId() + "; keyPrefix=" + apiKey.getKeyPrefix());
        return toClaimKeyResponse(apiKey, scopes, generatedKey.apiKey());
    }

    private ApiKeyClaimInvitation validateClaimRequest(ClaimKeyRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        String approvedEmail = requireText(request.getApprovedEmail(), "approvedEmail").toLowerCase();
        String claimCode = requireText(request.getClaimCode(), "claimCode");

        ApiKeyClaimInvitation invitation = invitationRepository.findLatestByApprovedEmail(approvedEmail)
                .orElseThrow(() -> new NotFoundException("Claim invitation not found"));

        if (invitation.getStatus() == ClaimInvitationStatus.LOCKED) {
            throw new ForbiddenException("Claim invitation locked");
        }
        if (invitation.getStatus() == ClaimInvitationStatus.CLAIMED) {
            throw new WebApplicationException("Claim invitation already claimed", Response.Status.CONFLICT);
        }
        if (invitation.getStatus() == ClaimInvitationStatus.REVOKED) {
            throw new ForbiddenException("Claim invitation revoked");
        }
        if (isExpired(invitation, Instant.now())) {
            invitation.setStatus(ClaimInvitationStatus.EXPIRED);
            invitationRepository.update(invitation);
            auditService.recordEvent(invitation.getClient().getId(), null, ApiKeyAuditEventType.AUTHENTICATION_FAILED,
                    CLAIM_ACTOR, "claim invitation expired; invitationId=" + invitation.getId());
            throw new WebApplicationException("Claim invitation expired", Response.Status.GONE);
        }
        if (invitation.getStatus() != ClaimInvitationStatus.PENDING) {
            throw new ForbiddenException("Claim invitation is not pending");
        }

        if (!claimCodeHasher.verify(claimCode, invitation.getClaimCodeHash())) {
            incrementFailedAttempt(invitation);
            auditService.recordEvent(invitation.getClient().getId(), null, ApiKeyAuditEventType.AUTHENTICATION_FAILED,
                    CLAIM_ACTOR,
                    "claim code verification failed; invitationId=" + invitation.getId()
                            + "; failedAttemptCount=" + invitation.getFailedAttemptCount());
            throw new NotAuthorizedException("Invalid claim code");
        }
        return invitation;
    }

    private ApiClient findClient(Long clientId) {
        if (clientId == null) {
            throw new BadRequestException("clientId is required");
        }
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("API client not found"));
    }

    private ApiKeyGenerator.GeneratedApiKey generateUniqueApiKey(ApiKeyClaimInvitation invitation) {
        for (int attempt = 0; attempt < MAX_KEY_GENERATION_ATTEMPTS; attempt++) {
            ApiKeyGenerator.GeneratedApiKey generatedKey = keyGenerator.generateApiKeyMaterial(invitation.getEnvironment());
            if (keyRepository.findByKeyPrefix(generatedKey.keyPrefix()).isEmpty()) {
                return generatedKey;
            }
        }
        throw new WebApplicationException("Unable to generate a unique API key prefix",
                Response.Status.INTERNAL_SERVER_ERROR);
    }

    private void persistScopes(ApiKey apiKey, List<String> scopes) {
        for (String scopeName : scopes) {
            ApiKeyScope scope = new ApiKeyScope();
            scope.setApiKey(apiKey);
            scope.setScope(scopeName);
            scopeRepository.persist(scope);
        }
    }

    private void incrementFailedAttempt(ApiKeyClaimInvitation invitation) {
        if (invitation.getMaxAttempts() <= 0) {
            invitation.setMaxAttempts(DEFAULT_MAX_CLAIM_ATTEMPTS);
        }
        int failedAttempts = invitation.getFailedAttemptCount() + 1;
        invitation.setFailedAttemptCount(failedAttempts);
        if (failedAttempts >= invitation.getMaxAttempts()) {
            invitation.setStatus(ClaimInvitationStatus.LOCKED);
            invitation.setLockedAt(Instant.now());
        }
        invitationRepository.update(invitation);
    }

    private ClaimInvitationCreatedResponse toInvitationResponse(
            ApiKeyClaimInvitation invitation,
            List<String> scopes,
            String rawClaimCode) {
        ClaimInvitationCreatedResponse response = new ClaimInvitationCreatedResponse();
        response.setInvitationId(invitation.getId());
        response.setClientId(invitation.getClient().getId());
        response.setApprovedEmail(invitation.getApprovedEmail());
        response.setKeyName(invitation.getKeyName());
        response.setEnvironment(invitation.getEnvironment());
        response.setScopes(scopes);
        response.setClaimCode(rawClaimCode);
        response.setApprovalReference(invitation.getApprovalReference());
        response.setCreatedAt(invitation.getCreatedAt());
        response.setExpiresAt(invitation.getExpiresAt());
        return response;
    }

    private ClaimKeyResponse toClaimKeyResponse(ApiKey apiKey, List<String> scopes, String rawApiKey) {
        ClaimKeyResponse response = new ClaimKeyResponse();
        response.setKeyId(apiKey.getId());
        response.setClientId(apiKey.getClient().getId());
        response.setKeyName(apiKey.getKeyName());
        response.setApiKey(rawApiKey);
        response.setKeyPrefix(apiKey.getKeyPrefix());
        response.setEnvironment(apiKey.getEnvironment());
        response.setScopes(scopes);
        response.setExpiresAt(apiKey.getExpiresAt());
        return response;
    }

    private ClaimReviewResponse toClaimReviewResponse(ApiKeyClaimInvitation invitation, List<String> scopes) {
        ClaimReviewResponse response = new ClaimReviewResponse();
        response.setInvitationId(invitation.getId());
        response.setClientId(invitation.getClient().getId());
        response.setClientName(invitation.getClient().getClientName());
        response.setApprovedEmail(invitation.getApprovedEmail());
        response.setKeyName(invitation.getKeyName());
        response.setEnvironment(invitation.getEnvironment());
        response.setScopes(scopes);
        response.setApprovalReference(invitation.getApprovalReference());
        response.setExpiresAt(invitation.getExpiresAt());
        return response;
    }

    private List<String> normalizeScopes(List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            throw new BadRequestException("At least one scope is required");
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String scope : scopes) {
            String scopeName = requireText(scope, "scope");
            if (!ALLOWED_SCOPES.contains(scopeName)) {
                throw new BadRequestException("Unsupported scope: " + scopeName);
            }
            normalized.add(scopeName);
        }
        return List.copyOf(normalized);
    }

    private boolean isExpired(ApiKeyClaimInvitation invitation, Instant now) {
        return invitation.getExpiresAt() != null && !invitation.getExpiresAt().isAfter(now);
    }

    private void requireRequest(CreateClaimInvitationRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
    }

    private String requireText(String value, String fieldName) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
