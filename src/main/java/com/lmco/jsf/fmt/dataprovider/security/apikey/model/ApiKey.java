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
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENT_ID", referencedColumnName = "ID", nullable = false)
    private ApiClient client;

    @Column(name = "KEY_PREFIX", nullable = false, unique = true, length = 64)
    private String keyPrefix;

    @Column(name = "KEY_HASH", nullable = false, length = 128)
    private String keyHash;

    @Column(name = "KEY_NAME", nullable = false, length = 255)
    private String keyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ENVIRONMENT", nullable = false, length = 32)
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 32)
    private ApiKeyStatus status;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "EXPIRES_AT")
    private Instant expiresAt;

    @Column(name = "REVOKED_AT")
    private Instant revokedAt;

    @Column(name = "REVOKED_BY", length = 255)
    private String revokedBy;

    @Column(name = "REVOCATION_REASON", length = 1000)
    private String revocationReason;

    @Column(name = "LAST_USED_AT")
    private Instant lastUsedAt;

    @Column(name = "ROTATION_GROUP_ID", length = 64)
    private String rotationGroupId;

    @Column(name = "ROTATION_INITIATED_AT")
    private Instant rotationInitiatedAt;

    @Column(name = "GRACE_PERIOD_ENDS_AT")
    private Instant gracePeriodEndsAt;

    @Column(name = "ROTATED_AT")
    private Instant rotatedAt;

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

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        this.keyHash = keyHash;
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

    public ApiKeyStatus getStatus() {
        return status;
    }

    public void setStatus(ApiKeyStatus status) {
        this.status = status;
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

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getRotationGroupId() {
        return rotationGroupId;
    }

    public void setRotationGroupId(String rotationGroupId) {
        this.rotationGroupId = rotationGroupId;
    }

    public Instant getRotationInitiatedAt() {
        return rotationInitiatedAt;
    }

    public void setRotationInitiatedAt(Instant rotationInitiatedAt) {
        this.rotationInitiatedAt = rotationInitiatedAt;
    }

    public Instant getGracePeriodEndsAt() {
        return gracePeriodEndsAt;
    }

    public void setGracePeriodEndsAt(Instant gracePeriodEndsAt) {
        this.gracePeriodEndsAt = gracePeriodEndsAt;
    }

    public Instant getRotatedAt() {
        return rotatedAt;
    }

    public void setRotatedAt(Instant rotatedAt) {
        this.rotatedAt = rotatedAt;
    }
}
