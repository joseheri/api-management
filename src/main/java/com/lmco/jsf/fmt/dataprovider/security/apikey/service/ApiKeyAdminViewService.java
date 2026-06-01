package com.lmco.jsf.fmt.dataprovider.security.apikey.service;

import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClient;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiClientStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKey;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEvent;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyAuditEventType;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyClaimInvitation;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ApiKeyStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.ClaimInvitationStatus;
import com.lmco.jsf.fmt.dataprovider.security.apikey.model.Environment;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiClientRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyAuditRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyClaimInvitationRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyRepository;
import com.lmco.jsf.fmt.dataprovider.security.apikey.repository.ApiKeyScopeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ApiKeyAdminViewService {

    private static final int DEFAULT_LIMIT = 500;
    private static final int RECENT_LIMIT = 8;
    private static final int EXPIRING_SOON_DAYS = 30;

    @Inject
    private ApiClientRepository clientRepository;

    @Inject
    private ApiKeyRepository keyRepository;

    @Inject
    private ApiKeyClaimInvitationRepository invitationRepository;

    @Inject
    private ApiKeyScopeRepository scopeRepository;

    @Inject
    private ApiKeyAuditRepository auditRepository;

    public AdminDashboard dashboard() {
        return new AdminDashboard(
                clientRepository.count(),
                clientRepository.countByStatus(ApiClientStatus.ACTIVE),
                keyRepository.count(),
                keyRepository.countByStatus(ApiKeyStatus.ACTIVE),
                keyRepository.countByStatus(ApiKeyStatus.REVOKED),
                invitationRepository.countByStatus(ClaimInvitationStatus.PENDING),
                keyRepository.countActiveExpiringBefore(Instant.now().plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS)),
                auditRepository.listRecent(0, RECENT_LIMIT).stream().map(this::toAuditRow).toList());
    }

    public List<ClientRow> listClients(ClientFilter filter) {
        ClientFilter safeFilter = filter == null ? ClientFilter.empty() : filter;
        return clientRepository.list(0, DEFAULT_LIMIT)
                .stream()
                .filter(client -> matchesClientStatus(client, safeFilter.status()))
                .map(this::toClientRow)
                .filter(row -> matchesText(row.searchableText(), safeFilter.search()))
                .filter(row -> !safeFilter.hasActiveKeys() || row.activeKeysCount() > 0)
                .filter(row -> !safeFilter.hasPendingInvitations() || row.pendingClaimsCount() > 0)
                .toList();
    }

    public ClientDetail clientDetail(Long clientId) {
        ApiClient client = findClient(clientId);
        ClientRow summary = toClientRow(client);
        List<KeyRow> keys = keyRepository.listByClientId(clientId, 0, DEFAULT_LIMIT)
                .stream()
                .map(this::toKeyRow)
                .toList();
        List<InvitationRow> pendingInvitations = invitationRepository
                .listByClientIdAndStatus(clientId, ClaimInvitationStatus.PENDING, 0, DEFAULT_LIMIT)
                .stream()
                .map(this::toInvitationRow)
                .toList();
        List<AuditRow> activity = auditRepository.listByClientId(clientId, 0, RECENT_LIMIT)
                .stream()
                .map(this::toAuditRow)
                .toList();
        return new ClientDetail(summary, keys, pendingInvitations, activity);
    }

    public List<KeyRow> listKeys(KeyFilter filter) {
        KeyFilter safeFilter = filter == null ? KeyFilter.empty() : filter;
        Instant expiresBefore = safeFilter.expiresWithinDays() == null
                ? null
                : Instant.now().plus(safeFilter.expiresWithinDays(), ChronoUnit.DAYS);
        return keyRepository.list(0, DEFAULT_LIMIT)
                .stream()
                .map(this::toKeyRow)
                .filter(row -> safeFilter.status() == null || row.status() == safeFilter.status())
                .filter(row -> safeFilter.environment() == null || row.environment() == safeFilter.environment())
                .filter(row -> safeFilter.scope() == null || row.scopes().contains(safeFilter.scope()))
                .filter(row -> expiresBefore == null
                        || row.expiresAt() != null && !row.expiresAt().isAfter(expiresBefore))
                .filter(row -> matchesText(row.searchableText(), safeFilter.search()))
                .toList();
    }

    public KeyDetail keyDetail(Long keyId) {
        ApiKey key = findKey(keyId);
        KeyRow keyRow = toKeyRow(key);
        ClientRow clientRow = toClientRow(key.getClient());
        List<AuditRow> activity = auditRepository.listByKeyId(keyId, 0, RECENT_LIMIT)
                .stream()
                .map(this::toAuditRow)
                .toList();
        return new KeyDetail(clientRow, keyRow, activity);
    }

    public List<InvitationRow> listPendingInvitations() {
        return invitationRepository.listByStatus(ClaimInvitationStatus.PENDING, 0, DEFAULT_LIMIT)
                .stream()
                .map(this::toInvitationRow)
                .toList();
    }

    private ApiClient findClient(Long clientId) {
        if (clientId == null) {
            throw new BadRequestException("clientId is required");
        }
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("API client not found"));
    }

    private ApiKey findKey(Long keyId) {
        if (keyId == null) {
            throw new BadRequestException("keyId is required");
        }
        return keyRepository.findById(keyId)
                .orElseThrow(() -> new NotFoundException("API key not found"));
    }

    private ClientRow toClientRow(ApiClient client) {
        long activeKeys = keyRepository.countByClientIdAndStatus(client.getId(), ApiKeyStatus.ACTIVE);
        long pendingClaims = invitationRepository.countByClientIdAndStatus(client.getId(), ClaimInvitationStatus.PENDING);
        Instant lastUsedAt = keyRepository.listByClientId(client.getId(), 0, DEFAULT_LIMIT)
                .stream()
                .map(ApiKey::getLastUsedAt)
                .filter(value -> value != null)
                .max(Instant::compareTo)
                .orElse(null);
        return new ClientRow(
                client.getId(),
                client.getClientName(),
                client.getOwner(),
                client.getContactEmail(),
                client.getStatus(),
                activeKeys,
                pendingClaims,
                lastUsedAt,
                client.getCreatedAt(),
                client.getUpdatedAt());
    }

    private KeyRow toKeyRow(ApiKey key) {
        ApiClient client = key.getClient();
        List<String> scopes = scopeRepository.listScopeNamesByApiKeyId(key.getId());
        return new KeyRow(
                key.getId(),
                client.getId(),
                client.getClientName(),
                client.getOwner(),
                key.getKeyName(),
                key.getKeyPrefix(),
                key.getEnvironment(),
                key.getStatus(),
                scopes,
                key.getCreatedAt(),
                key.getExpiresAt(),
                key.getLastUsedAt(),
                key.getRevokedAt(),
                key.getRevokedBy(),
                key.getRevocationReason(),
                key.getRotationGroupId(),
                key.getRotationInitiatedAt(),
                key.getGracePeriodEndsAt(),
                key.getRotatedAt());
    }

    private InvitationRow toInvitationRow(ApiKeyClaimInvitation invitation) {
        ApiClient client = invitation.getClient();
        return new InvitationRow(
                invitation.getId(),
                client.getId(),
                client.getClientName(),
                client.getOwner(),
                invitation.getApprovedEmail(),
                invitation.getKeyName(),
                invitation.getEnvironment(),
                invitation.getStatus(),
                List.copyOf(invitation.getScopes()),
                invitation.getApprovalReference(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt(),
                invitation.getClaimedAt(),
                invitation.getFailedAttemptCount(),
                invitation.getMaxAttempts(),
                invitation.getLockedAt());
    }

    private AuditRow toAuditRow(ApiKeyAuditEvent event) {
        return new AuditRow(
                event.getId(),
                event.getClientId(),
                event.getKeyId(),
                event.getEventType(),
                event.getPerformedBy(),
                event.getEventAt(),
                event.getDetails());
    }

    private boolean matchesClientStatus(ApiClient client, ApiClientStatus status) {
        return status == null || client.getStatus() == status;
    }

    private boolean matchesText(String text, String search) {
        String needle = normalize(search);
        return needle == null || normalize(text).contains(needle);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT).trim();
    }

    public record ClientFilter(String search, ApiClientStatus status, boolean hasActiveKeys, boolean hasPendingInvitations) {
        public static ClientFilter empty() {
            return new ClientFilter(null, null, false, false);
        }
    }

    public record KeyFilter(
            String search,
            ApiKeyStatus status,
            Environment environment,
            String scope,
            Integer expiresWithinDays) {
        public static KeyFilter empty() {
            return new KeyFilter(null, null, null, null, null);
        }
    }

    public record AdminDashboard(
            long clientsCount,
            long activeClientsCount,
            long keysCount,
            long activeKeysCount,
            long revokedKeysCount,
            long pendingClaimsCount,
            long expiringSoonCount,
            List<AuditRow> recentActivity) {
    }

    public record ClientRow(
            Long id,
            String clientName,
            String owner,
            String contactEmail,
            ApiClientStatus status,
            long activeKeysCount,
            long pendingClaimsCount,
            Instant lastUsedAt,
            Instant createdAt,
            Instant updatedAt) {
        public String searchableText() {
            return String.join(" ",
                    nullSafe(clientName),
                    nullSafe(owner),
                    nullSafe(contactEmail),
                    status == null ? "" : status.name());
        }
    }

    public record ClientDetail(
            ClientRow client,
            List<KeyRow> keys,
            List<InvitationRow> pendingInvitations,
            List<AuditRow> activity) {
    }

    public record KeyRow(
            Long id,
            Long clientId,
            String clientName,
            String clientOwner,
            String keyName,
            String keyPrefix,
            Environment environment,
            ApiKeyStatus status,
            List<String> scopes,
            Instant createdAt,
            Instant expiresAt,
            Instant lastUsedAt,
            Instant revokedAt,
            String revokedBy,
            String revocationReason,
            String rotationGroupId,
            Instant rotationInitiatedAt,
            Instant gracePeriodEndsAt,
            Instant rotatedAt) {
        public String searchableText() {
            return String.join(" ",
                    nullSafe(clientName),
                    nullSafe(clientOwner),
                    nullSafe(keyName),
                    nullSafe(keyPrefix),
                    environment == null ? "" : environment.name(),
                    status == null ? "" : status.name(),
                    String.join(" ", scopes));
        }
    }

    public record KeyDetail(ClientRow client, KeyRow key, List<AuditRow> activity) {
    }

    public record InvitationRow(
            Long id,
            Long clientId,
            String clientName,
            String clientOwner,
            String approvedEmail,
            String keyName,
            Environment environment,
            ClaimInvitationStatus status,
            List<String> scopes,
            String approvalReference,
            Instant createdAt,
            Instant expiresAt,
            Instant claimedAt,
            int failedAttemptCount,
            int maxAttempts,
            Instant lockedAt) {
    }

    public record AuditRow(
            Long id,
            Long clientId,
            Long keyId,
            ApiKeyAuditEventType eventType,
            String performedBy,
            Instant eventAt,
            String details) {
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
