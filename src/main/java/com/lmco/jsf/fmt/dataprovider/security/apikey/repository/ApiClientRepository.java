package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClient;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClientStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ApiClientRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiClient> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiClient.class, id));
    }

    public Optional<ApiClient> findByContactEmail(String contactEmail) {
        return entityManager.createQuery(
                        "select c from ApiClient c where lower(c.contactEmail) = lower(:contactEmail)",
                        ApiClient.class)
                .setParameter("contactEmail", contactEmail)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<ApiClient> list(int offset, int limit) {
        return entityManager.createQuery(
                        "select c from ApiClient c order by c.createdAt desc, c.id desc",
                        ApiClient.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiClient> listByStatus(ApiClientStatus status, int offset, int limit) {
        return entityManager.createQuery(
                        "select c from ApiClient c where c.status = :status order by c.createdAt desc, c.id desc",
                        ApiClient.class)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long count() {
        return entityManager.createQuery(
                        "select count(c) from ApiClient c",
                        Long.class)
                .getSingleResult();
    }

    public long countByStatus(ApiClientStatus status) {
        return entityManager.createQuery(
                        "select count(c) from ApiClient c where c.status = :status",
                        Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public void persist(ApiClient client) {
        entityManager.persist(client);
    }

    public ApiClient update(ApiClient client) {
        return entityManager.merge(client);
    }

    public void delete(ApiClient client) {
        entityManager.remove(entityManager.contains(client) ? client : entityManager.merge(client));
    }
}
