package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiKeyMetadataResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RevokeApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RotateApiKeyRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
public class ApiKeyService {

    @Inject
    private ApiKeyRotationService rotationService;

    public List<ApiKeyMetadataResponse> listKeysForClient(Long clientId) {
        throw notImplemented();
    }

    public ApiKeyMetadataResponse getKey(Long keyId) {
        throw notImplemented();
    }

    public ApiKeyMetadataResponse revokeKey(Long keyId, RevokeApiKeyRequest request) {
        throw notImplemented();
    }

    public ClaimInvitationCreatedResponse rotateKey(Long keyId, RotateApiKeyRequest request) {
        return rotationService.rotateKey(keyId, request);
    }

    private WebApplicationException notImplemented() {
        return new WebApplicationException("API key management is not implemented in Phase 1.",
                Response.Status.NOT_IMPLEMENTED);
    }
}
