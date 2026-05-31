package com.lmco.jsf.fmt.dataprovider.security.apikey.ui;

import com.lmco.jsf.fmt.dataprovider.security.apikey.config.ApiKeySecurityConfig;
import com.lmco.jsf.fmt.dataprovider.security.apikey.config.DevAdminGuard;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiClientResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiKeyMetadataResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateApiClientRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateClaimInvitationRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RevokeApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiClientService;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyClaimService;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class ApiKeyAdminUiResource {

    private static final List<String> AVAILABLE_SCOPES = List.of(
            "read:aircraft",
            "read:maintenance",
            "read:pairs",
            "read:usage");

    @Inject
    private ApiClientService apiClientService;

    @Inject
    private ApiKeyService apiKeyService;

    @Inject
    private ApiKeyClaimService apiKeyClaimService;

    @Inject
    private DevAdminGuard devAdminGuard;

    @Inject
    private ApiKeySecurityConfig securityConfig;

    @Inject
    private HttpHeaders headers;

    @Inject
    @Location("api-key-admin/client-list.html")
    private Template clientListTemplate;

    @Inject
    @Location("api-key-admin/client-new.html")
    private Template clientNewTemplate;

    @Inject
    @Location("api-key-admin/client-detail.html")
    private Template clientDetailTemplate;

    @Inject
    @Location("api-key-admin/invitation-new.html")
    private Template invitationNewTemplate;

    @Inject
    @Location("api-key-admin/invitation-created.html")
    private Template invitationCreatedTemplate;

    @Inject
    @Location("api-key-admin/key-detail.html")
    private Template keyDetailTemplate;

    @Inject
    @Location("api-key-admin/key-revoke.html")
    private Template keyRevokeTemplate;

    @Inject
    @Location("api-key-admin/error.html")
    private Template adminErrorTemplate;

    @Inject
    @Location("api-key-claim/claim-form.html")
    private Template claimFormTemplate;

    @Inject
    @Location("api-key-claim/claim-success.html")
    private Template claimSuccessTemplate;

    @Inject
    @Location("api-key-claim/claim-error.html")
    private Template claimErrorTemplate;

    @GET
    @Path("/admin/ui/api-clients")
    public Response listClients() {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        return html(Response.Status.OK, clientListTemplate
                .data("clients", apiClientService.listClients()));
    }

    @GET
    @Path("/admin/ui/api-clients/new")
    public Response newClientForm() {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        return html(Response.Status.OK, clientNewTemplate);
    }

    @POST
    @Path("/admin/ui/api-clients")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createClient(
            @FormParam("clientName") String clientName,
            @FormParam("contactEmail") String contactEmail,
            @FormParam("owner") String owner) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }

        try {
            CreateApiClientRequest request = new CreateApiClientRequest();
            request.setClientName(clientName);
            request.setContactEmail(contactEmail);
            request.setOwner(owner);
            ApiClientResponse client = apiClientService.createClient(request);
            return clientDetail(client.getId(), "API client created.");
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    @GET
    @Path("/admin/ui/api-clients/{clientId}")
    public Response clientDetail(@PathParam("clientId") Long clientId) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        return clientDetail(clientId, null);
    }

    @GET
    @Path("/admin/ui/api-clients/{clientId}/claim-invitations/new")
    public Response newInvitationForm(@PathParam("clientId") Long clientId) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }

        try {
            ApiClientResponse client = apiClientService.getClient(clientId);
            return html(Response.Status.OK, invitationNewTemplate
                    .data("client", client)
                    .data("environments", Environment.values())
                    .data("scopes", AVAILABLE_SCOPES)
                    .data("defaultExpiresAt", defaultExpiration()));
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    @POST
    @Path("/admin/ui/api-clients/{clientId}/claim-invitations")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createInvitation(
            @PathParam("clientId") Long clientId,
            @FormParam("approvedEmail") String approvedEmail,
            @FormParam("keyName") String keyName,
            @FormParam("environment") String environment,
            @FormParam("scopes") List<String> scopes,
            @FormParam("expiresAt") String expiresAt,
            @FormParam("approvalReference") String approvalReference) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }

        try {
            CreateClaimInvitationRequest request = new CreateClaimInvitationRequest();
            request.setApprovedEmail(approvedEmail);
            request.setKeyName(keyName);
            request.setEnvironment(Environment.valueOf(required(environment, "environment").toUpperCase()));
            request.setScopes(scopes);
            request.setExpiresAt(Instant.parse(required(expiresAt, "expiresAt")));
            request.setApprovalReference(approvalReference);
            ClaimInvitationCreatedResponse invitation = apiKeyClaimService.createInvitation(clientId, request);
            return html(Response.Status.CREATED, invitationCreatedTemplate
                    .data("invitation", invitation));
        } catch (IllegalArgumentException exception) {
            return adminError(Response.Status.BAD_REQUEST, exception.getMessage());
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    @GET
    @Path("/admin/ui/api-keys/{keyId}")
    public Response keyDetail(@PathParam("keyId") Long keyId) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        return keyDetail(keyId, null);
    }

    @GET
    @Path("/admin/ui/api-keys/{keyId}/revoke")
    public Response revokeKeyForm(@PathParam("keyId") Long keyId) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }

        try {
            ApiKeyMetadataResponse key = apiKeyService.getKey(keyId);
            return html(Response.Status.OK, keyRevokeTemplate
                    .data("key", key));
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    @POST
    @Path("/admin/ui/api-keys/{keyId}/revoke")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response revokeKey(
            @PathParam("keyId") Long keyId,
            @FormParam("reason") String reason) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }

        try {
            RevokeApiKeyRequest request = new RevokeApiKeyRequest();
            request.setReason(reason);
            ApiKeyMetadataResponse key = apiKeyService.revokeKey(keyId, request);
            return html(Response.Status.OK, keyDetailTemplate
                    .data("key", key)
                    .data("notice", "API key revoked."));
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    @GET
    @Path("/key-claim/ui")
    public Response claimForm() {
        return html(Response.Status.OK, claimFormTemplate);
    }

    @POST
    @Path("/key-claim/ui/complete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response completeClaim(
            @FormParam("approvedEmail") String approvedEmail,
            @FormParam("claimCode") String claimCode) {
        try {
            ClaimKeyRequest request = new ClaimKeyRequest();
            request.setApprovedEmail(approvedEmail);
            request.setClaimCode(claimCode);
            ClaimKeyResponse claimedKey = apiKeyClaimService.claimKey(request);
            return html(Response.Status.OK, claimSuccessTemplate
                    .data("claimedKey", claimedKey));
        } catch (WebApplicationException exception) {
            Response.Status status = Response.Status.fromStatusCode(exception.getResponse().getStatus());
            return html(status == null ? Response.Status.BAD_REQUEST : status, claimErrorTemplate
                    .data("message", exception.getMessage()));
        }
    }

    private Response clientDetail(Long clientId, String notice) {
        try {
            ApiClientResponse client = apiClientService.getClient(clientId);
            List<ApiKeyMetadataResponse> keys = apiKeyService.listKeysForClient(clientId);
            return html(Response.Status.OK, clientDetailTemplate
                    .data("client", client)
                    .data("keys", keys)
                    .data("notice", notice));
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    private Response keyDetail(Long keyId, String notice) {
        try {
            ApiKeyMetadataResponse key = apiKeyService.getKey(keyId);
            return html(Response.Status.OK, keyDetailTemplate
                    .data("key", key)
                    .data("notice", notice));
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    private Optional<Response> requireDevAdmin() {
        if (headers.getHeaderString(securityConfig.getApiKeyHeaderName()) != null) {
            return Optional.of(adminError(Response.Status.FORBIDDEN, "API keys cannot authorize admin UI routes."));
        }
        if (devAdminGuard.getDevAdminUser(headers).isEmpty()) {
            return Optional.of(adminError(Response.Status.UNAUTHORIZED,
                    "Admin UI requires the development/test header X-Dev-Admin-User. This is not production authentication."));
        }
        return Optional.empty();
    }

    private Response adminError(WebApplicationException exception) {
        Response.Status status = Response.Status.fromStatusCode(exception.getResponse().getStatus());
        return adminError(status == null ? Response.Status.BAD_REQUEST : status, exception.getMessage());
    }

    private Response adminError(Response.Status status, String message) {
        return html(status, adminErrorTemplate
                .data("message", message));
    }

    private Response html(Response.Status status, Template template) {
        return html(status, template.instance());
    }

    private Response html(Response.Status status, TemplateInstance instance) {
        return Response.status(status)
                .type(MediaType.TEXT_HTML_TYPE)
                .entity(instance)
                .build();
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String defaultExpiration() {
        return Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
