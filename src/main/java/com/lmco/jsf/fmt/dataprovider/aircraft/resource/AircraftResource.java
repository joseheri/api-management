package com.lmco.jsf.fmt.dataprovider.aircraft.resource;

import com.lmco.jsf.fmt.dataprovider.aircraft.model.Aircraft;
import com.lmco.jsf.fmt.dataprovider.aircraft.model.OperationalStatus;
import com.lmco.jsf.fmt.dataprovider.aircraft.service.AircraftService;
import com.lmco.jsf.fmt.dataprovider.aircraft.service.OperationalStatusService;
import com.lmco.jsf.fmt.dataprovider.common.filtering.TimeFilter;
import com.lmco.jsf.fmt.dataprovider.common.filtering.TimeFilterValidator;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PageRequest;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PaginationUtil;
import com.lmco.jsf.fmt.dataprovider.common.response.ApiResponse;
import com.lmco.jsf.fmt.dataprovider.security.apikey.annotation.RequiresScope;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Instant;

/**
 * REST resource for aircraft data endpoints.
 * 
 * Provides RESTful API for accessing aircraft information from ALIS:
 * - GET /api/v1/aircrafts - List all aircraft (paginated)
 * - GET /api/v1/aircrafts/{uai} - Get specific aircraft by UAI
 * - GET /api/v1/aircrafts?uai=... - Get multiple aircraft by UAI list
 * - GET /api/v1/aircrafts/uais - Get list of available UAIs
 * - GET /api/v1/aircrafts/{uai}/operational-statuses - Get operational statuses for aircraft
 * 
 * All endpoints exclude squadrons 'TR01' and 'LD01' per business rules.
 * Uses limit/offset pagination matching the OperationalStatus pattern.
 * 
 * @see Aircraft
 * @see AircraftService
 * @see OperationalStatus
 * @see OperationalStatusService
 */
@Path("/api/v1/aircrafts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Aircraft Resource", description = "Aircraft-specific endpoints")
public class AircraftResource {

    @Inject
    private AircraftService aircraftService;

    @Inject
    private OperationalStatusService operationalStatusService;

    /**
     * Get all aircraft or filter by UAI list with pagination.
     * 
     * Examples:
     * - GET /api/v1/aircraft?limit=100&offset=0
     * - GET /api/v1/aircraft?uai=AF001,AF002&limit=50&offset=0
     * 
     * @param uai Optional comma-separated list of UAIs to filter
     * @param limitParam Maximum number of records per page (default: 100, max: 500)
     * @param offsetParam Number of records to skip (default: 0)
     * @return ApiResponse with aircraft data, pagination info, and navigation links
     */
    @GET
    @RequiresScope("read:aircraft")
    @Operation(
        summary = "Retrieve aircraft data (all or filtered subset)",
        description = """
            Returns structured aircraft records in a paginated collection. Supports three query modes:
            
            ### 📋 All Aircraft Mode
            Returns all aircraft records (excluding TR01, LD01 squadrons):
            ```
            GET /api/v1/aircrafts?limit=100&offset=0
            ```
            
            ### 🔍 Filtered Subset Mode
            Returns filtered subset of aircraft records (1-to-many UAIs):
            ```
            GET /api/v1/aircrafts?uai=AF-001,AF-002,AF-003
            ```
            
            ### 🔍 Single Aircraft Mode
            Returns one aircraft record in paginated response:
            ```
            GET /api/v1/aircrafts?uai=AF-001
            ```
            
            ### 💡 When to Use This Endpoint
            - ✅ Need structured aircraft records (not just UAI strings)
            - ✅ Query all aircraft or a filtered subset by UAI list
            - ✅ Want pagination metadata and HATEOAS links
            - ✅ Need consistent ApiResponse wrapper structure
            
            ### 💡 Alternative: Direct Lookup
            For single aircraft without pagination overhead, use:
            ```
            GET /api/v1/aircrafts/{uai}
            ```
            (Returns raw Aircraft object, throws 404 if not found)
            
            **Response Structure:** Returns `ApiResponse<Aircraft>` with query metadata, pagination info, and navigation links.
            """
    )
    @APIResponse(responseCode = "200", description = "Successful response")
    @APIResponse(responseCode = "204", description = "No aircraft found matching criteria")
    @APIResponse(responseCode = "400", description = "Bad request - invalid parameters")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    public ApiResponse<Aircraft> getAllAircraft(
            @Parameter(
                name = "uai",
                description = """
                    **Optional:** Comma-separated list of UAIs to filter results.
                    
                    - Omit this parameter to get all aircraft
                    - Provide single UAI: `AF-001`
                    - Provide multiple UAIs: `AF-001,AF-002,AF-003`
                    - Whitespace is automatically trimmed
                    - Empty values are ignored
                    
                    **Examples:**
                    - `?uai=AF-001` → Returns 1 aircraft
                    - `?uai=AF-001,AF-002` → Returns 2 aircraft
                    - `?uai=AF-001, AF-002 , AF-003` → Returns 3 aircraft (spaces trimmed)
                    """,
                example = "AF-001,AF-002,AF-003",
                in = ParameterIn.QUERY
            )
            @QueryParam("uai") String uai,
            @Parameter(
                name = "limit",
                description = "Maximum number of records to return (default: 100, max: 1000)",
                example = "100",
                in = ParameterIn.QUERY
            )
            @QueryParam("limit") Integer limitParam,
            @Parameter(
                name = "offset",
                description = "Number of records to skip (default: 0)",
                example = "0",
                in = ParameterIn.QUERY
            )
            @QueryParam("offset") Integer offsetParam) {
        
        // Create page request with defaults
        PageRequest pageRequest = PaginationUtil.createPageRequest(limitParam, offsetParam);
        
        // If UAI parameter provided, filter by UAI list
        if (uai != null && !uai.trim().isEmpty()) {
            return aircraftService.getAircraftByUaiList(uai, pageRequest);
        }
        
        // Otherwise return all aircraft
        return aircraftService.getAllAircraft(pageRequest);
    }

    /**
     * Get a specific aircraft by its UAI (Unique Aircraft Identifier).
     * 
     * Example: GET /api/v1/aircraft/AF001
     * 
     * @param uai The unique aircraft identifier
     * @return Single aircraft data (not wrapped in ApiResponse)
     * @throws NotFoundException if aircraft not found (returns 404)
     */
    @GET
    @Path("/{uai}")
    @RequiresScope("read:aircraft")
    @Operation(
        summary = "Get specific aircraft by UAI (direct lookup)",
        description = """
            Direct lookup endpoint for retrieving a single aircraft by UAI.
            
            ### 🎯 Endpoint Characteristics
            - Returns raw `Aircraft` object (not wrapped in ApiResponse)
            - No pagination metadata or HATEOAS links
            - Throws 404 if aircraft not found
            - Lightweight and efficient for single aircraft retrieval
            
            ### 💡 When to Use This Endpoint
            - ✅ Need single aircraft data only
            - ✅ Want minimal response overhead
            - ✅ Prefer 404 error for missing aircraft
            
            ### 💡 Alternative: List Endpoint with UAI Filter
            If you need pagination metadata or HATEOAS links:
            ```
            GET /api/v1/aircrafts?uai=AF-001
            ```
            (Returns `ApiResponse<Aircraft>` structure with metadata)
            
            ### 📊 Response Comparison
            
            **This endpoint** (`GET /aircrafts/{uai}`):
            ```json
            {
              "sourceNode": "S014UNUS01",
              "uai": "AF-001",
              "eiui": "EIUI-F35A-002",
              ...
            }
            ```
            
            **List endpoint** (`GET /aircrafts?uai=AF-001`):
            ```json
            {
              "query": { ... },
              "data": [
                {
                  "sourceNode": "S014UNUS01",
                  "uai": "AF-001",
                  "eiui": "EIUI-F35A-002",
                  ...
                }
              ],
              "pagination": { ... },
              "links": { ... }
            }
            ```
            """
    )
    @APIResponse(responseCode = "200", description = "Successful response")
    @APIResponse(responseCode = "404", description = "Aircraft not found")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    public Aircraft getAircraftByUai(
            @Parameter(
                name = "uai",
                description = "Unique Aircraft Identifier",
                example = "AF-001",
                required = true,
                in = ParameterIn.PATH
            )
            @PathParam("uai") String uai) {
        return aircraftService.getAircraftByUai(uai);
    }

    /**
     * Get list of all available UAIs (Unique Aircraft Identifiers).
     * 
     * This is a lightweight endpoint that returns only UAI strings,
     * useful for populating dropdowns, autocomplete, or validation.
     * 
     * Example: GET /api/v1/aircraft/uais
     * 
     * Response format:
     * {
     *   "uais": ["AF001", "AF002", ...],
     *   "totalCount": 123,
     *   "filter": "Excludes squadrons TR01, LD01"
     * }
     * 
     * @return List of UAI strings with metadata (not wrapped in ApiResponse)
     */
    @GET
    @Path("/uais")
    @RequiresScope("read:aircraft")
    @Operation(
        summary = "Get list of aircraft identifiers (UAIs only)",
        description = """
            Returns a lightweight list of UAI strings only - **not full aircraft records**.
            
            ### 🎯 Endpoint Characteristics
            - Returns **only UAI strings** (e.g., ["AF-001", "AF-002", "AF-003"])
            - No structured aircraft data (no part numbers, locations, etc.)
            - No pagination (returns all UAIs in single response)
            - Optimized for dropdowns, autocomplete, and validation
            
            ### 💡 When to Use This Endpoint
            - ✅ Need list of available UAI values only
            - ✅ Populating dropdown menus or autocomplete fields
            - ✅ Client-side validation of UAI inputs
            - ✅ Want minimal response payload
            
            ### 💡 Alternative: Full Aircraft Records
            If you need structured aircraft data, use:
            ```
            GET /api/v1/aircrafts
            ```
            (Returns full aircraft records with all fields)
            
            **Response Format:**
            ```json
            {
              "uais": ["123", "AF-001", "AF-002", "AF-003"],
              "totalCount": 4,
              "filter": "Excludes squadrons TR01, LD01"
            }
            ```
            """
    )
    @APIResponse(responseCode = "200", description = "Successful response")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    public AircraftService.UaiListResponse getAllUais() {
        return aircraftService.getAllUais();
    }

    /**
     * Get operational statuses (mission capability) for specific aircraft.
     * 
     * Returns a paginated list of mission capability status records (FMC, PMC, NMC)
     * with support for time-based filtering and status filtering.
     * 
     * <p>Query Parameters:
     * <ul>
     *   <li>since - ISO 8601 timestamp for delta sync (e.g., 2026-05-01T00:00:00Z)</li>
     *   <li>from - ISO 8601 timestamp for range query start</li>
     *   <li>to - ISO 8601 timestamp for range query end</li>
     *   <li>status - Filter by status (FMC, PMC, or NMC)</li>
     *   <li>limit - Number of records per page (default: 100, max: 1000)</li>
     *   <li>offset - Starting position for pagination (default: 0)</li>
     * </ul>
     * 
     * <p>Response includes:
     * <ul>
     *   <li>query - Metadata about the query parameters used</li>
     *   <li>pagination - Total count, limit, offset, hasMore flag</li>
     *   <li>data - Array of operational status records</li>
     * </ul>
     * 
     * @param uai Unique Aircraft Identifier (required)
     * @param since ISO 8601 timestamp for delta sync (optional)
     * @param from ISO 8601 timestamp for range start (optional)
     * @param to ISO 8601 timestamp for range end (optional)
     * @param statusFilter Filter by status: FMC, PMC, or NMC (optional)
     * @param limit Number of records per page (default: 100)
     * @param offset Starting position (default: 0)
     * @return ApiResponse containing operational status records
     */
    @GET
    @Path("/{uai}/operational-statuses")
    @RequiresScope("read:aircraft")
    public Response getAircraftOperationalStatuses(
            @PathParam("uai") String uai,
            @QueryParam("since") String since,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("status") String statusFilter,
            @QueryParam("limit") Integer limitParam,
            @QueryParam("offset") Integer offsetParam) {
        
        // Validate and parse time filter parameters
        TimeFilterValidator validator = new TimeFilterValidator();
        TimeFilterValidator.ValidationResult validationResult = validator.validate(since, from, to);
        validationResult.throwIfInvalid();
        
        // Parse time parameters to Instant
        Instant sinceInstant = TimeFilterValidator.parseAndValidate(since, "since");
        Instant fromInstant = TimeFilterValidator.parseAndValidate(from, "from");
        Instant toInstant = TimeFilterValidator.parseAndValidate(to, "to");
        
        // Create time filter
        TimeFilter timeFilter = TimeFilter.from(sinceInstant, fromInstant, toInstant);
        
        // Create pagination request using PaginationUtil for consistency
        PageRequest pageRequest = PaginationUtil.createPageRequest(limitParam, offsetParam);
        
        // Call service layer
        ApiResponse<OperationalStatus> response = operationalStatusService.getOperationalStatuses(
                uai,
                timeFilter,
                statusFilter,
                pageRequest
        );
        
        // Return JAX-RS response
        return Response.ok(response).build();
    }
}
