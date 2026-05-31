package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyScope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyScopeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKeyScope> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKeyScope.class, id));
    }

    public Optional<ApiKeyScope> findByApiKeyIdAndScope(Long apiKeyId, String scope) {
        return entityManager.createQuery(
                        "select s from ApiKeyScope s where s.apiKey.id = :apiKeyId and s.scope = :scope",
                        ApiKeyScope.class)
                .setParameter("apiKeyId", apiKeyId)
                .setParameter("scope", scope)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<ApiKeyScope> listByApiKeyId(Long apiKeyId) {
        return entityManager.createQuery(
                        "select s from ApiKeyScope s where s.apiKey.id = :apiKeyId order by s.scope",
                        ApiKeyScope.class)
                .setParameter("apiKeyId", apiKeyId)
                .getResultList();
    }

    public List<String> listScopeNamesByApiKeyId(Long apiKeyId) {
        return entityManager.createQuery(
                        "select s.scope from ApiKeyScope s where s.apiKey.id = :apiKeyId order by s.scope",
                        String.class)
                .setParameter("apiKeyId", apiKeyId)
                .getResultList();
    }

    public long countByApiKeyId(Long apiKeyId) {
        return entityManager.createQuery(
                        "select count(s) from ApiKeyScope s where s.apiKey.id = :apiKeyId",
                        Long.class)
                .setParameter("apiKeyId", apiKeyId)
                .getSingleResult();
    }

    public int deleteByApiKeyId(Long apiKeyId) {
        return entityManager.createQuery(
                        "delete from ApiKeyScope s where s.apiKey.id = :apiKeyId")
                .setParameter("apiKeyId", apiKeyId)
                .executeUpdate();
    }

    public void persist(ApiKeyScope scope) {
        entityManager.persist(scope);
    }

    public ApiKeyScope update(ApiKeyScope scope) {
        return entityManager.merge(scope);
    }

    public void delete(ApiKeyScope scope) {
        entityManager.remove(entityManager.contains(scope) ? scope : entityManager.merge(scope));
    }
}
