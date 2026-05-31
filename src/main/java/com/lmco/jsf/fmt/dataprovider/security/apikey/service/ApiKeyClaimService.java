package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateClaimInvitationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ApiKeyClaimService {

    public ClaimInvitationCreatedResponse createInvitation(Long clientId, CreateClaimInvitationRequest request) {
        throw notImplemented();
    }

    public ClaimKeyResponse claimKey(ClaimKeyRequest request) {
        throw notImplemented();
    }

    private WebApplicationException notImplemented() {
        return new WebApplicationException("API key claim workflow is not implemented in Phase 1.",
                Response.Status.NOT_IMPLEMENTED);
    }
}
