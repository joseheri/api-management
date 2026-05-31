package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKey> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKey.class, id));
    }

    public void persist(ApiKey apiKey) {
        entityManager.persist(apiKey);
    }
}
