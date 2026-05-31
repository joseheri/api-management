package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.ApiClientResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.CreateApiClientRequest;
import com.lmco.jsf.fmt.dataprovider.security.apikey.dto.DisableApiClientRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
public class ApiClientService {

    public ApiClientResponse createClient(CreateApiClientRequest request) {
        throw notImplemented();
    }

    public List<ApiClientResponse> listClients() {
        throw notImplemented();
    }

    public ApiClientResponse getClient(Long clientId) {
        throw notImplemented();
    }

    public ApiClientResponse disableClient(Long clientId, DisableApiClientRequest request) {
        throw notImplemented();
    }

    public ApiClientResponse enableClient(Long clientId) {
        throw notImplemented();
    }

    private WebApplicationException notImplemented() {
        return new WebApplicationException("API key client management is not implemented in Phase 1.",
                Response.Status.NOT_IMPLEMENTED);
    }
}
