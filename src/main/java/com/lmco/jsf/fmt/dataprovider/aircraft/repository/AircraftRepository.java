package com.lmco.jsf.fmt.dataprovider.aircraft.repository;

import com.lmco.jsf.fmt.dataprovider.aircraft.model.Aircraft;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/**
 * TEMPORARY WORKSPACE-ONLY SHIM.
 *
 * Placeholder used only to satisfy compilation while the real aircraft
 * repository is absent from this workspace. Do not add SQL, EntityManager usage,
 * persistence mappings, or sensitive query logic here.
 */
@ApplicationScoped
public class AircraftRepository {

    public long count() {
        return 0L;
    }

    public List<Aircraft> findAllNative(int offset, int limit) {
        return List.of();
    }

    public Optional<Aircraft> findByUai(String uai) {
        return Optional.empty();
    }

    public long countByUaiIn(List<String> uais) {
        return 0L;
    }

    public List<Aircraft> findByUaiIn(List<String> uais, int offset, int limit) {
        return List.of();
    }

    public List<String> findAllUais() {
        return List.of();
    }
}
