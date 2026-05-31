package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyAuditRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKeyAuditEvent> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKeyAuditEvent.class, id));
    }

    public void persist(ApiKeyAuditEvent auditEvent) {
        entityManager.persist(auditEvent);
    }
}
