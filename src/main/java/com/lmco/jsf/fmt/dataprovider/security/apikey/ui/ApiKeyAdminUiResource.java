package com.lmco.jsf.fmt.dataprovider.security.apikey.ui;

import com.lmco.jsf.fmt.dataprovider.security.apikey.config.ApiKeySecurityConfig;
import com.lmco.jsf.fmt.dataprovider.security.apikey.config.DevAdminGuard;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiClientResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimInvitationCreatedResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimKeyResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ClaimReviewResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateApiClientRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateClaimInvitationRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.RevokeApiKeyRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClientStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyAdminViewService;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyAdminViewService.ClientFilter;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyAdminViewService.KeyFilter;
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
import jakarta.ws.rs.QueryParam;
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
    private ApiKeyAdminViewService adminViewService;

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
    @Location("api-key-admin/dashboard.html")
    private Template dashboardTemplate;

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
    @Location("api-key-admin/key-list.html")
    private Template keyListTemplate;

    @Inject
    @Location("api-key-admin/pending-invitations.html")
    private Template pendingInvitationsTemplate;

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
    @Location("api-key-claim/claim-review.html")
    private Template claimReviewTemplate;

    @Inject
    @Location("api-key-claim/claim-error.html")
    private Template claimErrorTemplate;

    @GET
    @Path("/admin/ui/api-clients")
    public Response listClients(
            @QueryParam("search") String search,
            @QueryParam("status") String status,
            @QueryParam("hasActiveKeys") boolean hasActiveKeys,
            @QueryParam("hasPendingInvitations") boolean hasPendingInvitations) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        try {
            ApiClientStatus clientStatus = parseClientStatus(status);
            return html(Response.Status.OK, clientListTemplate
                    .data("clients", adminViewService.listClients(new ClientFilter(
                            search,
                            clientStatus,
                            hasActiveKeys,
                            hasPendingInvitations)))
                    .data("search", search)
                    .data("status", status)
                    .data("hasActiveKeys", hasActiveKeys)
                    .data("hasPendingInvitations", hasPendingInvitations)
                    .data("clientStatuses", ApiClientStatus.values()));
        } catch (IllegalArgumentException exception) {
            return adminError(Response.Status.BAD_REQUEST, exception.getMessage());
        }
    }

    @GET
    @Path("/admin/ui")
    public Response dashboard() {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        return html(Response.Status.OK, dashboardTemplate
                .data("dashboard", adminViewService.dashboard()));
    }

    @GET
    @Path("/admin/ui/dashboard")
    public Response dashboardAlias() {
        return dashboard();
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
    @Path("/admin/ui/api-keys")
    public Response keyInventory(
            @QueryParam("search") String search,
            @QueryParam("status") String status,
            @QueryParam("environment") String environment,
            @QueryParam("scope") String scope,
            @QueryParam("expiresWithinDays") Integer expiresWithinDays) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        try {
            ApiKeyStatus keyStatus = parseKeyStatus(status);
            Environment keyEnvironment = parseEnvironment(environment);
            return html(Response.Status.OK, keyListTemplate
                    .data("keys", adminViewService.listKeys(new KeyFilter(
                            search,
                            keyStatus,
                            keyEnvironment,
                            blankToNull(scope),
                            expiresWithinDays)))
                    .data("search", search)
                    .data("status", status)
                    .data("environment", environment)
                    .data("scope", scope)
                    .data("expiresWithinDays", expiresWithinDays)
                    .data("keyStatuses", ApiKeyStatus.values())
                    .data("environments", Environment.values())
                    .data("scopes", AVAILABLE_SCOPES));
        } catch (IllegalArgumentException exception) {
            return adminError(Response.Status.BAD_REQUEST, exception.getMessage());
        }
    }

    @GET
    @Path("/admin/ui/claim-invitations")
    public Response pendingInvitations() {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }
        return html(Response.Status.OK, pendingInvitationsTemplate
                .data("invitations", adminViewService.listPendingInvitations()));
    }

    @GET
    @Path("/admin/ui/api-keys/{keyId}/revoke")
    public Response revokeKeyForm(@PathParam("keyId") Long keyId) {
        Optional<Response> adminFailure = requireDevAdmin();
        if (adminFailure.isPresent()) {
            return adminFailure.get();
        }

        try {
            return html(Response.Status.OK, keyRevokeTemplate
                    .data("detail", adminViewService.keyDetail(keyId)));
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
            apiKeyService.revokeKey(keyId, request);
            return keyDetail(keyId, "API key revoked.");
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
    @Path("/key-claim/ui/review")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response reviewClaim(
            @FormParam("approvedEmail") String approvedEmail,
            @FormParam("claimCode") String claimCode) {
        try {
            ClaimKeyRequest request = new ClaimKeyRequest();
            request.setApprovedEmail(approvedEmail);
            request.setClaimCode(claimCode);
            ClaimReviewResponse claimReview = apiKeyClaimService.reviewClaim(request);
            return html(Response.Status.OK, claimReviewTemplate
                    .data("claimReview", claimReview)
                    .data("approvedEmail", approvedEmail)
                    .data("claimCode", claimCode));
        } catch (WebApplicationException exception) {
            return claimError(exception);
        }
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
            return claimError(exception);
        }
    }

    private Response clientDetail(Long clientId, String notice) {
        try {
            return html(Response.Status.OK, clientDetailTemplate
                    .data("detail", adminViewService.clientDetail(clientId))
                    .data("notice", notice));
        } catch (WebApplicationException exception) {
            return adminError(exception);
        }
    }

    private Response keyDetail(Long keyId, String notice) {
        try {
            return html(Response.Status.OK, keyDetailTemplate
                    .data("detail", adminViewService.keyDetail(keyId))
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

    private Response claimError(WebApplicationException exception) {
        Response.Status status = Response.Status.fromStatusCode(exception.getResponse().getStatus());
        Response.Status safeStatus = status == null ? Response.Status.BAD_REQUEST : status;
        return html(safeStatus, claimErrorTemplate
                .data("message", claimErrorMessage(safeStatus, exception)));
    }

    private String claimErrorMessage(Response.Status status, WebApplicationException exception) {
        if (status == Response.Status.UNAUTHORIZED || status == Response.Status.NOT_FOUND) {
            return "Invalid claim information. Check the approved identifier and one-time claim code, then try again.";
        }
        if (status == Response.Status.GONE) {
            return "Claim invitation expired. Contact your internal administrator for a new claim invitation.";
        }
        if (status == Response.Status.CONFLICT) {
            return "Claim invitation already used. Contact your internal administrator if you need a replacement.";
        }
        if (status == Response.Status.FORBIDDEN) {
            String message = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
            if (message.contains("locked")) {
                return "Claim invitation locked. Contact your internal administrator for assistance.";
            }
        }
        return "Unable to complete claim. Contact your internal administrator if the problem continues.";
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

    private ApiClientStatus parseClientStatus(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : ApiClientStatus.valueOf(trimmed.toUpperCase());
    }

    private ApiKeyStatus parseKeyStatus(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : ApiKeyStatus.valueOf(trimmed.toUpperCase());
    }

    private Environment parseEnvironment(String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : Environment.valueOf(trimmed.toUpperCase());
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String defaultExpiration() {
        return Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString();
    }
}
