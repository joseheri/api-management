package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClaimCodeHasher {

    public String hash(String rawClaimCode) {
        throw new UnsupportedOperationException("Claim code hashing is not implemented in Phase 1.");
    }

    public boolean verify(String rawClaimCode, String storedHash) {
        // Phase 1 skeleton only. BCrypt verification is implemented in a later phase.
        return false;
    }
}
