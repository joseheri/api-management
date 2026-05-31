package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyScope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyScopeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKeyScope> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKeyScope.class, id));
    }

    public void persist(ApiKeyScope scope) {
        entityManager.persist(scope);
    }
}
