package com.lmco.jsf.fmt.dataprovider.security.apikey.resource;

import com.lmco.jsf.fmt.dataprovider.common.response.ApiResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiKeyMetadataResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateClaimInvitationRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RevokeApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RotateApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyClaimService;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/v1/admin/api-keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiKeyManagementResource {

    @Inject
    private ApiKeyService apiKeyService;

    @Inject
    private ApiKeyClaimService apiKeyClaimService;

    @POST
    @Path("/clients/{clientId}/invitations")
    public Response createInvitation(
            @PathParam("clientId") Long clientId,
            CreateClaimInvitationRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(apiKeyClaimService.createInvitation(clientId, request))
                .build();
    }

    @GET
    @Path("/clients/{clientId}")
    public Response listKeysForClient(@PathParam("clientId") Long clientId) {
        List<ApiKeyMetadataResponse> keys = apiKeyService.listKeysForClient(clientId);
        return Response.ok(ApiResponse.of(keys)).build();
    }

    @GET
    @Path("/{keyId}")
    public Response getKey(@PathParam("keyId") Long keyId) {
        return Response.ok(apiKeyService.getKey(keyId)).build();
    }

    @POST
    @Path("/{keyId}/revoke")
    public Response revokeKey(@PathParam("keyId") Long keyId, RevokeApiKeyRequest request) {
        return Response.ok(apiKeyService.revokeKey(keyId, request)).build();
    }

    @POST
    @Path("/{keyId}/rotate")
    public Response rotateKey(@PathParam("keyId") Long keyId, RotateApiKeyRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(apiKeyService.rotateKey(keyId, request))
                .build();
    }
}
