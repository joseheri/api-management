package com.lmco.jsf.fmt.dataprovider.aircraft.repository;

import com.lmco.jsf.fmt.dataprovider.aircraft.model.Aircraft;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/**
 * TEMPORARY CODEX WORKSPACE SHIM.
 *
 * Placeholder used only to satisfy compilation while the real aircraft
 * repository is absent from this workspace. Do not add SQL, EntityManager usage,
 * persistence mappings, or sensitive query logic here.
 */
@ApplicationScoped
public class AircraftRepository {

    private static final List<Aircraft> PLACEHOLDER_AIRCRAFT = List.of(
            new Aircraft("PLACEHOLDER-001", "workspace-shim", "F-35", "AVAILABLE"));

    public long count() {
        return PLACEHOLDER_AIRCRAFT.size();
    }

    public List<Aircraft> findAllNative(int offset, int limit) {
        return page(PLACEHOLDER_AIRCRAFT, offset, limit);
    }

    public Optional<Aircraft> findByUai(String uai) {
        return PLACEHOLDER_AIRCRAFT.stream()
                .filter(aircraft -> aircraft.getUai().equalsIgnoreCase(uai))
                .findFirst();
    }

    public long countByUaiIn(List<String> uais) {
        return filterByUaiIn(uais).size();
    }

    public List<Aircraft> findByUaiIn(List<String> uais, int offset, int limit) {
        return page(filterByUaiIn(uais), offset, limit);
    }

    public List<String> findAllUais() {
        return PLACEHOLDER_AIRCRAFT.stream()
                .map(Aircraft::getUai)
                .toList();
    }

    private List<Aircraft> filterByUaiIn(List<String> uais) {
        if (uais == null || uais.isEmpty()) {
            return List.of();
        }
        return PLACEHOLDER_AIRCRAFT.stream()
                .filter(aircraft -> uais.stream().anyMatch(uai -> aircraft.getUai().equalsIgnoreCase(uai)))
                .toList();
    }

    private List<Aircraft> page(List<Aircraft> aircraft, int offset, int limit) {
        if (aircraft.isEmpty() || limit <= 0 || offset >= aircraft.size()) {
            return List.of();
        }

        int safeOffset = Math.max(offset, 0);
        int safeLimit = Math.max(limit, 0);
        int toIndex = Math.min(safeOffset + safeLimit, aircraft.size());
        return aircraft.subList(safeOffset, toIndex);
    }
}
