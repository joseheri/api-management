package com.lmco.jsf.fmt.dataprovider.security.apikey.resource;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyClaimService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/api-keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiKeyClaimResource {

    @Inject
    private ApiKeyClaimService apiKeyClaimService;

    @POST
    @Path("/claim")
    public Response claimKey(ClaimKeyRequest request) {
        return Response.ok(apiKeyClaimService.claimKey(request)).build();
    }
}
