package com.lmco.jsf.fmt.dataprovider.security.apikey.repository;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyClaimInvitation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class ApiKeyClaimInvitationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ApiKeyClaimInvitation> findById(Long id) {
        return Optional.ofNullable(entityManager.find(ApiKeyClaimInvitation.class, id));
    }

    public void persist(ApiKeyClaimInvitation invitation) {
        entityManager.persist(invitation);
    }
}
