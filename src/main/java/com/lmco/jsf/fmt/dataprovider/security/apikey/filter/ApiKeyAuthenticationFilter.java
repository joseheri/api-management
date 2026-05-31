package com.lmco.jsf.fmt.dataprovider.security.apikey.filter;

import com.lmco.jsf.fmt.dataprovider.common.correlation.CorrelationIdContext;
import com.lmco.jsf.fmt.dataprovider.common.error.ErrorResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.config.ApiKeySecurityConfig;
import com.lmco.jsf.fmt.dataprovider.security.apikey.config.DevAdminGuard;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyValidator;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyValidator.AuthenticatedApiKey;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyAuthenticationFilter implements ContainerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String API_KEY_AUTH_SCHEME = "API_KEY";
    public static final String DEV_ADMIN_AUTH_SCHEME = "DEV_ADMIN";

    @Inject
    private ApiKeySecurityConfig securityConfig;

    @Inject
    private ApiKeyValidator apiKeyValidator;

    @Inject
    private DevAdminGuard devAdminGuard;

    @Inject
    private ApiKeyRequestContext apiKeyRequestContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        apiKeyRequestContext.clear();

        String path = normalizedPath(requestContext);
        if (isAdminApiPath(path)) {
            handleAdminRequest(requestContext);
            return;
        }

        if (isApiKeyExemptPath(path)) {
            return;
        }

        String rawApiKey = requestContext.getHeaderString(securityConfig.getApiKeyHeaderName());
        if (rawApiKey == null || rawApiKey.isBlank()) {
            return;
        }

        Optional<AuthenticatedApiKey> authenticatedApiKey = apiKeyValidator.authenticate(rawApiKey);
        if (authenticatedApiKey.isEmpty()) {
            abort(requestContext, Response.Status.UNAUTHORIZED, ErrorResponse.ErrorCodes.API_KEY_INVALID,
                    "API key authentication failed");
            return;
        }

        AuthenticatedApiKey apiKey = authenticatedApiKey.get();
        apiKeyRequestContext.setAuthenticatedApiKey(apiKey);
        requestContext.setSecurityContext(apiKeySecurityContext(requestContext.getSecurityContext(), apiKey));
    }

    private void handleAdminRequest(ContainerRequestContext requestContext) {
        if (hasHeader(requestContext, securityConfig.getApiKeyHeaderName())) {
            abort(requestContext, Response.Status.FORBIDDEN, ErrorResponse.ErrorCodes.FORBIDDEN,
                    "API keys cannot authorize admin endpoints");
            return;
        }

        Optional<String> devAdminUser = devAdminGuard.getDevAdminUser(requestContext);
        if (devAdminUser.isEmpty()) {
            abort(requestContext, Response.Status.UNAUTHORIZED, ErrorResponse.ErrorCodes.UNAUTHORIZED,
                    "Admin authentication required");
            return;
        }

        apiKeyRequestContext.setDevAdminUser(devAdminUser.get());
        requestContext.setSecurityContext(devAdminSecurityContext(requestContext.getSecurityContext(), devAdminUser.get()));
    }

    private SecurityContext apiKeySecurityContext(SecurityContext delegate, AuthenticatedApiKey authenticatedApiKey) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> "api-key:" + authenticatedApiKey.keyPrefix();
            }

            @Override
            public boolean isUserInRole(String role) {
                return authenticatedApiKey.scopes().contains(role);
            }

            @Override
            public boolean isSecure() {
                return delegate != null && delegate.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return API_KEY_AUTH_SCHEME;
            }
        };
    }

    private SecurityContext devAdminSecurityContext(SecurityContext delegate, String devAdminUser) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> devAdminUser;
            }

            @Override
            public boolean isUserInRole(String role) {
                return "dev-admin".equals(role);
            }

            @Override
            public boolean isSecure() {
                return delegate != null && delegate.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return DEV_ADMIN_AUTH_SCHEME;
            }
        };
    }

    private boolean isAdminApiPath(String path) {
        return path.equals("/api/v1/admin") || path.startsWith("/api/v1/admin/");
    }

    private boolean isApiKeyExemptPath(String path) {
        return path.equals("/api/v1/api-keys/claim")
                || path.startsWith("/health")
                || path.startsWith("/metrics")
                || path.startsWith("/q/health")
                || path.startsWith("/q/metrics")
                || path.startsWith("/q/openapi")
                || path.startsWith("/q/swagger-ui")
                || path.startsWith("/openapi")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars/")
                || path.startsWith("/assets/")
                || path.startsWith("/static/")
                || path.startsWith("/admin/ui")
                || path.startsWith("/key-claim/ui")
                || path.equals("/favicon.ico")
                || path.equals("/robots.txt");
    }

    private boolean hasHeader(ContainerRequestContext requestContext, String headerName) {
        return requestContext.getHeaders().containsKey(headerName);
    }

    private String normalizedPath(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath(false);
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private void abort(
            ContainerRequestContext requestContext,
            Response.Status status,
            String errorCode,
            String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode)
                .message(message)
                .requestId(CorrelationIdContext.get())
                .build();

        requestContext.abortWith(Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build());
    }
}
