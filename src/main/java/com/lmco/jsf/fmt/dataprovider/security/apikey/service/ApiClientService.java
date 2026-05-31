package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiClientResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateApiClientRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.DisableApiClientRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClient;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClientStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiClientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class ApiClientService {

    private static final int DEFAULT_LIST_LIMIT = 500;
    private static final String SYSTEM_ACTOR = "system";

    @Inject
    private ApiClientRepository clientRepository;

    @Inject
    private ApiKeyAuditService auditService;

    @Transactional
    public ApiClientResponse createClient(CreateApiClientRequest request) {
        requireRequest(request);
        String clientName = requireText(request.getClientName(), "clientName");
        String contactEmail = requireText(request.getContactEmail(), "contactEmail").toLowerCase();

        clientRepository.findByContactEmail(contactEmail)
                .ifPresent(existing -> {
                    throw new WebApplicationException(
                            "An API client already exists for the provided contact email.",
                            Response.Status.CONFLICT);
                });

        Instant now = Instant.now();
        ApiClient client = new ApiClient();
        client.setClientName(clientName);
        client.setContactEmail(contactEmail);
        client.setOwner(trimToNull(request.getOwner()));
        client.setStatus(ApiClientStatus.ACTIVE);
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        clientRepository.persist(client);

        auditService.recordEvent(client.getId(), null, ApiKeyAuditEventType.CLIENT_CREATED, SYSTEM_ACTOR,
                "clientName=" + client.getClientName());
        return toResponse(client);
    }

    public List<ApiClientResponse> listClients() {
        return clientRepository.list(0, DEFAULT_LIST_LIMIT)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ApiClientResponse getClient(Long clientId) {
        return toResponse(findClient(clientId));
    }

    @Transactional
    public ApiClientResponse disableClient(Long clientId, DisableApiClientRequest request) {
        ApiClient client = findClient(clientId);
        if (client.getStatus() != ApiClientStatus.DISABLED) {
            client.setStatus(ApiClientStatus.DISABLED);
            client.setUpdatedAt(Instant.now());
            clientRepository.update(client);
            auditService.recordEvent(client.getId(), null, ApiKeyAuditEventType.CLIENT_DISABLED, SYSTEM_ACTOR,
                    "reason=" + safeReason(request == null ? null : request.getReason()));
        }
        return toResponse(client);
    }

    @Transactional
    public ApiClientResponse enableClient(Long clientId) {
        ApiClient client = findClient(clientId);
        if (client.getStatus() != ApiClientStatus.ACTIVE) {
            client.setStatus(ApiClientStatus.ACTIVE);
            client.setUpdatedAt(Instant.now());
            clientRepository.update(client);
            auditService.recordEvent(client.getId(), null, ApiKeyAuditEventType.CLIENT_ENABLED, SYSTEM_ACTOR);
        }
        return toResponse(client);
    }

    private ApiClient findClient(Long clientId) {
        if (clientId == null) {
            throw new BadRequestException("clientId is required");
        }
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("API client not found"));
    }

    private ApiClientResponse toResponse(ApiClient client) {
        ApiClientResponse response = new ApiClientResponse();
        response.setId(client.getId());
        response.setClientName(client.getClientName());
        response.setContactEmail(client.getContactEmail());
        response.setOwner(client.getOwner());
        response.setStatus(client.getStatus());
        response.setCreatedAt(client.getCreatedAt());
        response.setUpdatedAt(client.getUpdatedAt());
        return response;
    }

    private void requireRequest(CreateApiClientRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
    }

    private String requireText(String value, String fieldName) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String safeReason(String reason) {
        String trimmed = trimToNull(reason);
        return trimmed == null ? "not provided" : trimmed;
    }
}
