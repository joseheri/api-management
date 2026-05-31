package com.lmco.jsf.fmt.dataprovider.security.apikey.interceptor;

import com.lmco.jsf.fmt.dataprovider.security.apikey.annotation.RequiresScope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.SecurityContext;

@ApplicationScoped
public class ScopeEnforcementInterceptor {

    public boolean hasRequiredScope(SecurityContext securityContext, RequiresScope requiresScope) {
        // Phase 1 skeleton only. Scope authorization is implemented in a later phase.
        return false;
    }
}
