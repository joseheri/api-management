package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClaimCodeHasher {

    private static final int BCRYPT_COST = 12;

    public String hash(String rawClaimCode) {
        if (rawClaimCode == null || rawClaimCode.isBlank()) {
            throw new IllegalArgumentException("Claim code is required");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, rawClaimCode.toCharArray());
    }

    public boolean verify(String rawClaimCode, String storedHash) {
        if (rawClaimCode == null || rawClaimCode.isBlank() || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return BCrypt.verifyer().verify(rawClaimCode.toCharArray(), storedHash).verified;
    }
}
