package com.lmco.jsf.fmt.dataprovider.security.apikey.interceptor;

import com.lmco.jsf.fmt.dataprovider.common.correlation.CorrelationIdContext;
import com.lmco.jsf.fmt.dataprovider.common.error.ErrorResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.annotation.RequiresScope;
import com.lmco.jsf.fmt.dataprovider.security.apikey.config.ApiKeySecurityConfig;
import com.lmco.jsf.fmt.dataprovider.security.apikey.filter.ApiKeyAuthenticationFilter;
import com.lmco.jsf.fmt.dataprovider.security.apikey.filter.ApiKeyRequestContext;
import com.lmco.jsf.fmt.dataprovider.security.apikey.service.ApiKeyValidator.AuthenticatedApiKey;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Optional;
import org.jboss.logging.Logger;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHORIZATION)
public class ScopeEnforcementInterceptor implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(ScopeEnforcementInterceptor.class);

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private ApiKeySecurityConfig securityConfig;

    @Inject
    private ApiKeyRequestContext apiKeyRequestContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        RequiresScope requiresScope = findRequiresScope();
        if (requiresScope == null) {
            return;
        }

        Optional<AuthenticatedApiKey> authenticatedApiKey = apiKeyRequestContext.getAuthenticatedApiKey();
        if (authenticatedApiKey.isEmpty()) {
            if (!securityConfig.isEnforcementEnabled()) {
                if (securityConfig.isReportOnly() && !hasReportOnlyInvalidKey(requestContext)) {
                    LOG.warnf("API key report-only: request would fail with 401, reason=missing API key, method=%s, path=%s, requiredScope=%s, correlationId=%s",
                            requestContext.getMethod(), normalizedPath(requestContext), requiresScope.value(),
                            CorrelationIdContext.get());
                }
                return;
            }

            abort(requestContext, Response.Status.UNAUTHORIZED, ErrorResponse.ErrorCodes.API_KEY_MISSING,
                    "API key authentication required");
            return;
        }

        String requiredScope = requiresScope.value();
        if (!authenticatedApiKey.get().scopes().contains(requiredScope)) {
            if (!securityConfig.isEnforcementEnabled()) {
                if (securityConfig.isReportOnly()) {
                    LOG.warnf("API key report-only: request would fail with 403, reason=missing required scope, method=%s, path=%s, keyPrefix=%s, requiredScope=%s, correlationId=%s",
                            requestContext.getMethod(), normalizedPath(requestContext),
                            authenticatedApiKey.get().keyPrefix(), requiredScope, CorrelationIdContext.get());
                }
                return;
            }

            abort(requestContext, Response.Status.FORBIDDEN, ErrorResponse.ErrorCodes.API_KEY_SCOPE_MISSING,
                    "API key does not have the required scope");
        }
    }

    public boolean hasRequiredScope(SecurityContext securityContext, RequiresScope requiresScope) {
        return securityContext != null
                && requiresScope != null
                && securityContext.isUserInRole(requiresScope.value());
    }

    private RequiresScope findRequiresScope() {
        if (resourceInfo == null) {
            return null;
        }

        Method method = resourceInfo.getResourceMethod();
        if (method != null && method.isAnnotationPresent(RequiresScope.class)) {
            return method.getAnnotation(RequiresScope.class);
        }

        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (resourceClass != null && resourceClass.isAnnotationPresent(RequiresScope.class)) {
            return resourceClass.getAnnotation(RequiresScope.class);
        }

        return null;
    }

    private boolean hasReportOnlyInvalidKey(ContainerRequestContext requestContext) {
        return Boolean.TRUE.equals(requestContext.getProperty(ApiKeyAuthenticationFilter.REPORT_ONLY_INVALID_KEY_PROPERTY));
    }

    private String normalizedPath(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
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
