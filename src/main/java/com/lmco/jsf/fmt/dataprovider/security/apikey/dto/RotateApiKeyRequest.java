package com.lmco.jsf.fmt.dataprovider.security.apikey.dto;

import java.time.Instant;

public class RotateApiKeyRequest {

    private Instant replacementExpiresAt;
    private String reason;

    public Instant getReplacementExpiresAt() {
        return replacementExpiresAt;
    }

    public void setReplacementExpiresAt(Instant replacementExpiresAt) {
        this.replacementExpiresAt = replacementExpiresAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
