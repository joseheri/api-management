package com.lmco.jsf.fmt.dataprovider.security.apikey.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "api_key_claim_invitations")
public class ApiKeyClaimInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENT_ID", nullable = false)
    private ApiClient client;

    @Column(name = "APPROVED_EMAIL", nullable = false, length = 255)
    private String approvedEmail;

    @Column(name = "CLAIM_CODE_HASH", nullable = false, length = 128)
    private String claimCodeHash;

    @Column(name = "KEY_NAME", nullable = false, length = 255)
    private String keyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ENVIRONMENT", nullable = false, length = 32)
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 32)
    private ClaimInvitationStatus status;

    @Column(name = "APPROVAL_REFERENCE", length = 255)
    private String approvalReference;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private Instant expiresAt;

    @Column(name = "CLAIMED_AT")
    private Instant claimedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApiClient getClient() {
        return client;
    }

    public void setClient(ApiClient client) {
        this.client = client;
    }

    public String getApprovedEmail() {
        return approvedEmail;
    }

    public void setApprovedEmail(String approvedEmail) {
        this.approvedEmail = approvedEmail;
    }

    public String getClaimCodeHash() {
        return claimCodeHash;
    }

    public void setClaimCodeHash(String claimCodeHash) {
        this.claimCodeHash = claimCodeHash;
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

    public ClaimInvitationStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimInvitationStatus status) {
        this.status = status;
    }

    public String getApprovalReference() {
        return approvalReference;
    }

    public void setApprovalReference(String approvalReference) {
        this.approvalReference = approvalReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(Instant claimedAt) {
        this.claimedAt = claimedAt;
    }
}
