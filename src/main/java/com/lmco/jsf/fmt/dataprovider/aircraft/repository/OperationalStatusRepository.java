package com.lmco.jsf.fmt.dataprovider.aircraft.repository;

import com.lmco.jsf.fmt.dataprovider.aircraft.model.OperationalStatus;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;

/**
 * TEMPORARY WORKSPACE-ONLY SHIM.
 *
 * Placeholder used only to satisfy compilation while the real operational
 * status repository is absent from this workspace. Do not add SQL,
 * EntityManager usage, persistence mappings, or sensitive query logic here.
 */
@ApplicationScoped
public class OperationalStatusRepository {

    public boolean existsByUai(String uai) {
        return false;
    }

    public List<OperationalStatus> findByUaiWithFilters(
            String uai,
            Instant since,
            Instant from,
            Instant to,
            String statusFilter,
            int limit,
            int offset) {
        return List.of();
    }

    public long countByUaiWithFilters(
            String uai,
            Instant since,
            Instant from,
            Instant to,
            String statusFilter) {
        return 0L;
    }
}
