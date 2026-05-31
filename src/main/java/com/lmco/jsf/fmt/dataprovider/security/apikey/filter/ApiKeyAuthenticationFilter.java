package com.lmco.jsf.fmt.dataprovider.security.apikey.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyAuthenticationFilter implements ContainerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Phase 1 skeleton only. Runtime API-key authentication is implemented in a later phase.
    }
}
