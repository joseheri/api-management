package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiKeyGenerator {

    public String generateApiKey(Environment environment) {
        throw new UnsupportedOperationException("API key generation is not implemented in Phase 1.");
    }

    public String generateClaimCode() {
        throw new UnsupportedOperationException("Claim code generation is not implemented in Phase 1.");
    }
}
