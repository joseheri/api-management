package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class ApiClientRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiClient> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiClient.class, id));
    }

    public void persist(ApiClient client) {
        entityManager.persist(client);
    }
}
