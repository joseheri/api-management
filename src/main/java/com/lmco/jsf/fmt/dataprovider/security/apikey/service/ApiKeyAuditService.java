package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEvent;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyAuditRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class ApiKeyAuditService {

    private static final int MAX_DETAILS_LENGTH = 4000;

    @Inject
    private ApiKeyAuditRepository auditRepository;

    public void recordEvent(Long clientId, Long keyId, ApiKeyAuditEventType eventType, String performedBy) {
        recordEvent(clientId, keyId, eventType, performedBy, null);
    }

    @Transactional
    public void recordEvent(
            Long clientId,
            Long keyId,
            ApiKeyAuditEventType eventType,
            String performedBy,
            String details) {
        ApiKeyAuditEvent event = new ApiKeyAuditEvent();
        event.setClientId(clientId);
        event.setKeyId(keyId);
        event.setEventType(eventType);
        event.setPerformedBy(clean(performedBy));
        event.setEventAt(Instant.now());
        event.setDetails(truncate(clean(details), MAX_DETAILS_LENGTH));
        auditRepository.persist(event);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\r\\n\\t]", " ").trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
