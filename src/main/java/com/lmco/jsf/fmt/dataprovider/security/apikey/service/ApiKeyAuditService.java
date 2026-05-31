package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiKeyAuditService {

    public void recordEvent(Long clientId, Long keyId, ApiKeyAuditEventType eventType, String performedBy) {
        // Phase 1 skeleton only. Audit persistence is implemented in a later phase.
    }
}
