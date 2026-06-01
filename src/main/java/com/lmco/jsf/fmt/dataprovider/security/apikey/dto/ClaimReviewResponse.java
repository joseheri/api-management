package com.lmco.jsf.fmt.dataprovider.security.apikey.dto;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import java.time.Instant;
import java.util.List;

public class ClaimReviewResponse {

    private Long invitationId;
    private Long clientId;
    private String clientName;
    private String approvedEmail;
    private String keyName;
    private Environment environment;
    private List<String> scopes;
    private String approvalReference;
    private Instant expiresAt;

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getApprovedEmail() {
        return approvedEmail;
    }

    public void setApprovedEmail(String approvedEmail) {
        this.approvedEmail = approvedEmail;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getApprovalReference() {
        return approvalReference;
    }

    public void setApprovalReference(String approvalReference) {
        this.approvalReference = approvalReference;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
