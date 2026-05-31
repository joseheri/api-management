package com.lmco.jsf.fmt.dataprovider.security.apikey.interceptor;

import com.lmco.jsf.fmt.dataprovider.common.correlation.CorrelationIdContext;
import com.lmco.jsf.fmt.dataprovider.common.error.ErrorResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.annotation.RequiresScope;
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

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHORIZATION)
public class ScopeEnforcementInterceptor implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

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
            abort(requestContext, Response.Status.UNAUTHORIZED, ErrorResponse.ErrorCodes.API_KEY_MISSING,
                    "API key authentication required");
            return;
        }

        String requiredScope = requiresScope.value();
        if (!authenticatedApiKey.get().scopes().contains(requiredScope)) {
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
