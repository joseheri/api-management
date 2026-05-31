package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RotateApiKeyRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ApiKeyRotationService {

    public ClaimInvitationCreatedResponse rotateKey(Long keyId, RotateApiKeyRequest request) {
        throw new WebApplicationException("API key rotation is not implemented in Phase 1.",
                Response.Status.NOT_IMPLEMENTED);
    }
}
