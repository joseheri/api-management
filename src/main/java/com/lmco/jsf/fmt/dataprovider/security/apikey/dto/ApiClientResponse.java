package com.lmco.jsf.fmt.dataprovider.security.apikey.dto;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClientStatus;
import java.time.Instant;

public class ApiClientResponse {

    private Long id;
    private String clientName;
    private String contactEmail;
    private ApiClientStatus status;
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public ApiClientStatus getStatus() {
        return status;
    }

    public void setStatus(ApiClientStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
