package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyAuditRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKeyAuditEvent> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKeyAuditEvent.class, id));
    }

    public List<ApiKeyAuditEvent> listByClientId(Long clientId, int offset, int limit) {
        return entityManager.createQuery(
                        "select a from ApiKeyAuditEvent a "
                                + "where a.clientId = :clientId "
                                + "order by a.eventAt desc, a.id desc",
                        ApiKeyAuditEvent.class)
                .setParameter("clientId", clientId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiKeyAuditEvent> listByKeyId(Long keyId, int offset, int limit) {
        return entityManager.createQuery(
                        "select a from ApiKeyAuditEvent a "
                                + "where a.keyId = :keyId "
                                + "order by a.eventAt desc, a.id desc",
                        ApiKeyAuditEvent.class)
                .setParameter("keyId", keyId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiKeyAuditEvent> listByClientIdAndKeyId(Long clientId, Long keyId, int offset, int limit) {
        return entityManager.createQuery(
                        "select a from ApiKeyAuditEvent a "
                                + "where a.clientId = :clientId and a.keyId = :keyId "
                                + "order by a.eventAt desc, a.id desc",
                        ApiKeyAuditEvent.class)
                .setParameter("clientId", clientId)
                .setParameter("keyId", keyId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countByClientId(Long clientId) {
        return entityManager.createQuery(
                        "select count(a) from ApiKeyAuditEvent a where a.clientId = :clientId",
                        Long.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public long countByKeyId(Long keyId) {
        return entityManager.createQuery(
                        "select count(a) from ApiKeyAuditEvent a where a.keyId = :keyId",
                        Long.class)
                .setParameter("keyId", keyId)
                .getSingleResult();
    }

    public long countByClientIdAndKeyId(Long clientId, Long keyId) {
        return entityManager.createQuery(
                        "select count(a) from ApiKeyAuditEvent a where a.clientId = :clientId and a.keyId = :keyId",
                        Long.class)
                .setParameter("clientId", clientId)
                .setParameter("keyId", keyId)
                .getSingleResult();
    }

    public void persist(ApiKeyAuditEvent auditEvent) {
        entityManager.persist(auditEvent);
    }

    public ApiKeyAuditEvent update(ApiKeyAuditEvent auditEvent) {
        return entityManager.merge(auditEvent);
    }

    public void delete(ApiKeyAuditEvent auditEvent) {
        entityManager.remove(entityManager.contains(auditEvent) ? auditEvent : entityManager.merge(auditEvent));
    }
}
