package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyClaimInvitation;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ClaimInvitationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyClaimInvitationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKeyClaimInvitation> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKeyClaimInvitation.class, id));
    }

    public Optional<ApiKeyClaimInvitation> findByApprovedEmailAndStatus(
            String approvedEmail,
            ClaimInvitationStatus status) {
        return entityManager.createQuery(
                        "select i from ApiKeyClaimInvitation i "
                                + "where lower(i.approvedEmail) = lower(:approvedEmail) and i.status = :status "
                                + "order by i.createdAt desc, i.id desc",
                        ApiKeyClaimInvitation.class)
                .setParameter("approvedEmail", approvedEmail)
                .setParameter("status", status)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<ApiKeyClaimInvitation> findLatestByApprovedEmail(String approvedEmail) {
        return entityManager.createQuery(
                        "select i from ApiKeyClaimInvitation i "
                                + "where lower(i.approvedEmail) = lower(:approvedEmail) "
                                + "order by i.createdAt desc, i.id desc",
                        ApiKeyClaimInvitation.class)
                .setParameter("approvedEmail", approvedEmail)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<ApiKeyClaimInvitation> listByClientId(Long clientId, int offset, int limit) {
        return entityManager.createQuery(
                        "select i from ApiKeyClaimInvitation i "
                                + "where i.client.id = :clientId "
                                + "order by i.createdAt desc, i.id desc",
                        ApiKeyClaimInvitation.class)
                .setParameter("clientId", clientId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiKeyClaimInvitation> listByClientIdAndStatus(
            Long clientId,
            ClaimInvitationStatus status,
            int offset,
            int limit) {
        return entityManager.createQuery(
                        "select i from ApiKeyClaimInvitation i "
                                + "where i.client.id = :clientId and i.status = :status "
                                + "order by i.createdAt desc, i.id desc",
                        ApiKeyClaimInvitation.class)
                .setParameter("clientId", clientId)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ApiKeyClaimInvitation> listByStatus(ClaimInvitationStatus status, int offset, int limit) {
        return entityManager.createQuery(
                        "select i from ApiKeyClaimInvitation i join fetch i.client "
                                + "where i.status = :status "
                                + "order by i.createdAt desc, i.id desc",
                        ApiKeyClaimInvitation.class)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countByClientId(Long clientId) {
        return entityManager.createQuery(
                        "select count(i) from ApiKeyClaimInvitation i where i.client.id = :clientId",
                        Long.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public long countByClientIdAndStatus(Long clientId, ClaimInvitationStatus status) {
        return entityManager.createQuery(
                        "select count(i) from ApiKeyClaimInvitation i "
                                + "where i.client.id = :clientId and i.status = :status",
                        Long.class)
                .setParameter("clientId", clientId)
                .setParameter("status", status)
                .getSingleResult();
    }

    public long countByStatus(ClaimInvitationStatus status) {
        return entityManager.createQuery(
                        "select count(i) from ApiKeyClaimInvitation i where i.status = :status",
                        Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public void persist(ApiKeyClaimInvitation invitation) {
        entityManager.persist(invitation);
    }

    public ApiKeyClaimInvitation update(ApiKeyClaimInvitation invitation) {
        return entityManager.merge(invitation);
    }

    public void delete(ApiKeyClaimInvitation invitation) {
        entityManager.remove(entityManager.contains(invitation) ? invitation : entityManager.merge(invitation));
    }
}
