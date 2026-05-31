package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKey> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKey.class, id));
    }

    public Optional<ApiKey> findByKeyPrefix(String keyPrefix) {
        return entityManager.createQuery(
                        "select k from ApiKey k where k.keyPrefix = :keyPrefix",
                        ApiKey.class)
                .setParameter("keyPrefix", keyPrefix)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<ApiKey> listByClientId(Long clientId, int offset, int limit) {
        return entityManager.createQuery(
                        "select k from ApiKey k where k.client.id = :clientId order by k.createdAt desc, k.id desc",
                        ApiKey.class)
                .setParameter("clientId", clientId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiKey> listByClientIdAndStatus(Long clientId, ApiKeyStatus status, int offset, int limit) {
        return entityManager.createQuery(
                        "select k from ApiKey k "
                                + "where k.client.id = :clientId and k.status = :status "
                                + "order by k.createdAt desc, k.id desc",
                        ApiKey.class)
                .setParameter("clientId", clientId)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiKey> listExpiredActiveKeys(int offset, int limit) {
        return entityManager.createQuery(
                        "select k from ApiKey k "
                                + "where k.status = :status and k.expiresAt is not null and k.expiresAt <= CURRENT_TIMESTAMP "
                                + "order by k.expiresAt asc, k.id asc",
                        ApiKey.class)
                .setParameter("status", ApiKeyStatus.ACTIVE)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiKey> listRotationGraceEndedActiveKeys(int offset, int limit) {
        return entityManager.createQuery(
                        "select k from ApiKey k "
                                + "where k.status = :status "
                                + "and k.rotationInitiatedAt is not null "
                                + "and k.gracePeriodEndsAt is not null "
                                + "and k.gracePeriodEndsAt <= CURRENT_TIMESTAMP "
                                + "order by k.gracePeriodEndsAt asc, k.id asc",
                        ApiKey.class)
                .setParameter("status", ApiKeyStatus.ACTIVE)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countByClientId(Long clientId) {
        return entityManager.createQuery(
                        "select count(k) from ApiKey k where k.client.id = :clientId",
                        Long.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public long countByClientIdAndStatus(Long clientId, ApiKeyStatus status) {
        return entityManager.createQuery(
                        "select count(k) from ApiKey k where k.client.id = :clientId and k.status = :status",
                        Long.class)
                .setParameter("clientId", clientId)
                .setParameter("status", status)
                .getSingleResult();
    }

    public void persist(ApiKey apiKey) {
        entityManager.persist(apiKey);
    }

    public ApiKey update(ApiKey apiKey) {
        return entityManager.merge(apiKey);
    }

    public void delete(ApiKey apiKey) {
        entityManager.remove(entityManager.contains(apiKey) ? apiKey : entityManager.merge(apiKey));
    }
}
