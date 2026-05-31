package com.lmco.jsf.fmt.dataprovider.aircraft.service;

import com.lmco.jsf.fmt.dataprovider.aircraft.model.OperationalStatus;
import com.lmco.jsf.fmt.dataprovider.aircraft.repository.OperationalStatusRepository;
import com.lmco.jsf.fmt.dataprovider.common.filtering.TimeFilter;
import com.lmco.jsf.fmt.dataprovider.common.pagination.NavigationLinks;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PageRequest;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PaginationInfo;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PaginationUtil;
import com.lmco.jsf.fmt.dataprovider.common.response.ApiResponse;
import com.lmco.jsf.fmt.dataprovider.common.response.QueryInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer for aircraft operational status (mission capability) operations.
 * 
 * Orchestrates business logic and integrates Option A components:
 * - TimeFilter for time-based filtering
 * - PageRequest for pagination parameters
 * - PageResponse for pagination metadata
 * - ApiResponse for consistent response structure
 * - QueryInfo for query metadata
 * 
 * Validates inputs and coordinates with the repository layer to provide
 * mission capability status data (FMC, PMC, NMC) for aircraft.
 * 
 * @see com.lmco.jsf.fmt.dataprovider.aircraft.repository.OperationalStatusRepository
 * @see com.lmco.jsf.fmt.dataprovider.aircraft.resource.AircraftResource
 */
@ApplicationScoped
public class OperationalStatusService {

    private static final Logger LOGGER = Logger.getLogger(OperationalStatusService.class.getName());

    @Inject
    OperationalStatusRepository repository;

    /**
     * Get operational statuses for a specific aircraft with optional filtering.
     * 
     * Supports:
     * - Delta sync (since parameter)
     * - Range queries (from/to parameters)
     * - Status filtering (FMC, PMC, NMC)
     * - Pagination (limit/offset)
     * 
     * @param uai Unique Aircraft Identifier (required)
     * @param timeFilter Time filtering parameters (since, from, to)
     * @param statusFilter Specific status to filter by (FMC, PMC, NMC) (optional)
     * @param pageRequest Pagination parameters (limit, offset)
     * @return ApiResponse containing query metadata, pagination info, and data
     * @throws IllegalArgumentException if UAI is null or empty
     * @throws jakarta.persistence.NoResultException if aircraft does not exist
     */
    public ApiResponse<OperationalStatus> getOperationalStatuses(
            String uai,
            TimeFilter timeFilter,
            String statusFilter,
            PageRequest pageRequest) {

        LOGGER.info(() -> String.format("Fetching operational statuses for UAI: %s, timeFilter: %s, statusFilter: %s, pageRequest: %s",
                uai, timeFilter, statusFilter, pageRequest));

        // Validate required parameters
        validateUai(uai);
        validateAircraftExists(uai);

        // Extract time filter parameters
        Instant since = timeFilter.isDelta() ? timeFilter.getSince() : null;
        Instant from = timeFilter.isRange() ? timeFilter.getFrom() : null;
        Instant to = timeFilter.isRange() ? timeFilter.getTo() : null;

        // Validate status filter if provided
        if (statusFilter != null && !statusFilter.isEmpty()) {
            validateStatusFilter(statusFilter);
        }

        // Query data with pagination
        List<OperationalStatus> data = repository.findByUaiWithFilters(
                uai,
                since,
                from,
                to,
                statusFilter,
                pageRequest.getLimit(),
                pageRequest.getOffset()
        );

        // Get total count for pagination
        long totalCount = repository.countByUaiWithFilters(
                uai,
                since,
                from,
                to,
                statusFilter
        );

        // Build response components
        QueryInfo queryInfo = QueryInfo.from(pageRequest, timeFilter);
        PaginationInfo paginationInfo = PaginationUtil.createPaginationInfo(totalCount, pageRequest);
        
        // Build HATEOAS navigation links
        String basePath = "/api/v1/aircrafts/" + uai + "/operational-statuses";
        NavigationLinks links = PaginationUtil.createNavigationLinks(basePath, pageRequest, paginationInfo);

        LOGGER.info(() -> String.format("Found %d operational statuses (total: %d) for UAI: %s",
                data.size(), totalCount, uai));

        // Return clean, flat API response structure
        return new ApiResponse<>(queryInfo, data, paginationInfo, links);
    }

    /**
     * Validate that UAI is not null or empty.
     * 
     * @param uai Unique Aircraft Identifier to validate
     * @throws IllegalArgumentException if UAI is null or empty
     */
    private void validateUai(String uai) {
        if (uai == null || uai.trim().isEmpty()) {
            LOGGER.warning("UAI validation failed: UAI is null or empty");
            throw new IllegalArgumentException("UAI (Unique Aircraft Identifier) is required");
        }
    }

    /**
     * Validate that the aircraft exists in the system.
     * 
     * @param uai Unique Aircraft Identifier
     * @throws IllegalArgumentException if aircraft does not exist
     */
    private void validateAircraftExists(String uai) {
        if (!repository.existsByUai(uai)) {
            LOGGER.warning(() -> String.format("Aircraft validation failed: UAI '%s' not found", uai));
            throw new IllegalArgumentException(
                    String.format("Aircraft with UAI '%s' not found in the system", uai));
        }
    }

    /**
     * Validate that status filter is a valid mission capability status.
     * 
     * @param statusFilter Status filter to validate
     * @throws IllegalArgumentException if status is not FMC, PMC, or NMC
     */
    private void validateStatusFilter(String statusFilter) {
        try {
            OperationalStatus.MissionCapabilityStatus.valueOf(statusFilter.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warning(() -> String.format("Invalid status filter: %s. Valid values are: FMC, PMC, NMC",
                    statusFilter));
            throw new IllegalArgumentException(
                    String.format("Invalid status filter '%s'. Valid values are: FMC, PMC, NMC", statusFilter));
        }
    }

    /**
     * Check if an aircraft exists in the system.
     * 
     * @param uai Unique Aircraft Identifier
     * @return true if aircraft exists, false otherwise
     */
    public boolean aircraftExists(String uai) {
        return repository.existsByUai(uai);
    }
}
