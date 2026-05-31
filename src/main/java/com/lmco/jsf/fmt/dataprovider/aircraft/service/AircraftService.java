package com.lmco.jsf.fmt.dataprovider.aircraft.service;

import com.lmco.jsf.fmt.dataprovider.aircraft.model.Aircraft;
import com.lmco.jsf.fmt.dataprovider.aircraft.repository.AircraftRepository;
import com.lmco.jsf.fmt.dataprovider.common.pagination.NavigationLinks;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PageRequest;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PaginationInfo;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PaginationUtil;
import com.lmco.jsf.fmt.dataprovider.common.response.ApiResponse;
import com.lmco.jsf.fmt.dataprovider.common.response.QueryInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Aircraft operations.
 * 
 * Provides business logic for aircraft data retrieval including:
 * - Paginated list of all aircraft
 * - Single aircraft lookup by UAI
 * - Multiple aircraft lookup by UAI list
 * - UAI-only list retrieval
 * 
 * Handles pagination metadata generation and response wrapping using ApiResponse.
 * 
 * @see Aircraft
 * @see AircraftRepository
 * @see com.lmco.jsf.fmt.dataprovider.aircraft.resource.AircraftResource
 */
@ApplicationScoped
public class AircraftService {

    @Inject
    private AircraftRepository aircraftRepository;

    /**
     * Maximum allowed page size to prevent performance issues
     */
    private static final int MAX_LIMIT = 500;

    /**
     * Get all aircraft with pagination.
     * 
     * @param pageRequest Pagination parameters (limit, offset)
     * @return ApiResponse with aircraft data and pagination metadata
     */
    public ApiResponse<Aircraft> getAllAircraft(PageRequest pageRequest) {
        // Validate limit
        int limit = Math.min(pageRequest.getLimit(), MAX_LIMIT);
        int offset = pageRequest.getOffset();
        
        // Get total count for pagination metadata
        long totalCount = aircraftRepository.count();
        
        // Fetch page of data
        List<Aircraft> aircraft = aircraftRepository.findAllNative(offset, limit);
        
        // Build response components
        QueryInfo queryInfo = QueryInfo.from(pageRequest, null);
        PaginationInfo paginationInfo = PaginationUtil.createPaginationInfo(totalCount, pageRequest);
        
        // Build HATEOAS navigation links
        NavigationLinks links = PaginationUtil.createNavigationLinks(
            "/api/v1/aircraft",
            pageRequest,
            paginationInfo
        );
        
        // Return flat API response structure
        return new ApiResponse<>(queryInfo, aircraft, paginationInfo, links);
    }

    /**
     * Get a single aircraft by its UAI (Unique Aircraft Identifier).
     * 
     * @param uai The unique aircraft identifier
     * @return Aircraft data
     * @throws NotFoundException if aircraft with given UAI is not found
     */
    public Aircraft getAircraftByUai(String uai) {
        return aircraftRepository.findByUai(uai)
            .orElseThrow(() -> new NotFoundException("Aircraft with UAI '" + uai + "' not found"));
    }

    /**
     * Get multiple aircraft by list of UAIs with pagination.
     * 
     * @param uais Comma-separated list of UAIs
     * @param pageRequest Pagination parameters (limit, offset)
     * @return ApiResponse with matching aircraft and pagination metadata
     */
    public ApiResponse<Aircraft> getAircraftByUaiList(String uais, PageRequest pageRequest) {
        // Parse comma-separated UAI list
        List<String> uaiList = parseUaiList(uais);
        
        if (uaiList.isEmpty()) {
            // Return empty response if no UAIs provided
            QueryInfo queryInfo = QueryInfo.from(pageRequest, null);
            PaginationInfo emptyPagination = new PaginationInfo(0L, pageRequest.getLimit(), pageRequest.getOffset());
            return new ApiResponse<>(queryInfo, List.of(), emptyPagination, null);
        }
        
        // Validate limit
        int limit = Math.min(pageRequest.getLimit(), MAX_LIMIT);
        int offset = pageRequest.getOffset();
        
        // Get total count for pagination metadata
        long totalCount = aircraftRepository.countByUaiIn(uaiList);
        
        // Fetch page of data
        List<Aircraft> aircraft = aircraftRepository.findByUaiIn(uaiList, offset, limit);
        
        // Build response components
        QueryInfo queryInfo = QueryInfo.from(pageRequest, null);
        PaginationInfo paginationInfo = new PaginationInfo(totalCount, limit, offset);
        
        // Build HATEOAS navigation links with uai parameter preserved
        String baseUrl = "/api/v1/aircraft?uai=" + uais;
        NavigationLinks links = PaginationUtil.createNavigationLinks(
            baseUrl,
            pageRequest,
            paginationInfo
        );
        
        // Return flat API response structure
        return new ApiResponse<>(queryInfo, aircraft, paginationInfo, links);
    }

    /**
     * Get list of all available UAIs (Unique Aircraft Identifiers).
     * This is a lightweight endpoint returning only UAI strings.
     * 
     * @return List response with UAI strings and metadata
     */
    public UaiListResponse getAllUais() {
        List<String> uais = aircraftRepository.findAllUais();
        
        return new UaiListResponse(
            uais,
            uais.size(),
            "Excludes squadrons TR01, LD01"
        );
    }

    /**
     * Parse comma-separated UAI list into List<String>.
     * Trims whitespace and filters out empty strings.
     * 
     * @param uais Comma-separated UAI string
     * @return List of UAI strings
     */
    private List<String> parseUaiList(String uais) {
        if (uais == null || uais.trim().isEmpty()) {
            return List.of();
        }
        
        return Arrays.stream(uais.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Response wrapper for UAI list endpoint.
     * Provides metadata about the UAI list.
     */
    public static class UaiListResponse {
        private final List<String> uais;
        private final int totalCount;
        private final String filter;

        public UaiListResponse(List<String> uais, int totalCount, String filter) {
            this.uais = uais;
            this.totalCount = totalCount;
            this.filter = filter;
        }

        public List<String> getUais() {
            return uais;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public String getFilter() {
            return filter;
        }
    }
}
