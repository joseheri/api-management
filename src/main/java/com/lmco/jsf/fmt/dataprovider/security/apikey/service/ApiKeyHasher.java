package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiKeyHasher {

    public String hmacSha256(String rawApiKey) {
        throw new UnsupportedOperationException("API key hashing is not implemented in Phase 1.");
    }
}
