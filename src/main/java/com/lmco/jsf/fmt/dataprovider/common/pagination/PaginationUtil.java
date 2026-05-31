package com.lmco.jsf.fmt.dataprovider.common.pagination;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for pagination operations.
 * 
 * <p>Provides helper methods for:
 * <ul>
 *   <li>Building HATEOAS navigation links</li>
 *   <li>Calculating pagination offsets</li>
 *   <li>URL encoding query parameters</li>
 *   <li>Creating PageResponse objects</li>
 * </ul>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public final class PaginationUtil {
    
    // Private constructor to prevent instantiation
    private PaginationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Creates HATEOAS navigation links using independent NavigationLinks class.
     * 
     * @param baseUrl the base URL of the endpoint (may include existing query parameters)
     * @param pageRequest the current page request
     * @param paginationInfo the pagination metadata
     * @return navigation links, or null if pagination info is null
     */
    public static NavigationLinks createNavigationLinks(
            String baseUrl,
            PageRequest pageRequest,
            PaginationInfo paginationInfo) {
        
        if (paginationInfo == null) {
            return null;
        }
        
        int limit = pageRequest.getLimit();
        int offset = pageRequest.getOffset();
        long totalCount = paginationInfo.getTotalCount();
        
        // Determine separator: use & if baseUrl already has query params, otherwise use ?
        String separator = baseUrl.contains("?") ? "&" : "?";
        
        // Build links with correct separator
        String selfLink = baseUrl + separator + "limit=" + limit + "&offset=" + offset;
        
        String nextLink = paginationInfo.hasNext()
            ? baseUrl + separator + "limit=" + limit + "&offset=" + (offset + limit)
            : null;
        
        String prevLink = paginationInfo.hasPrevious()
            ? baseUrl + separator + "limit=" + limit + "&offset=" + Math.max(0, offset - limit)
            : null;
        
        String firstLink = baseUrl + separator + "limit=" + limit + "&offset=0";
        
        int lastOffset = calculateLastPageOffset(totalCount, limit);
        String lastLink = baseUrl + separator + "limit=" + limit + "&offset=" + lastOffset;
        
        return new NavigationLinks(selfLink, nextLink, prevLink, firstLink, lastLink);
    }
    
    /**
     * Builds HATEOAS navigation links for a paginated response.
     * 
     * @param baseUrl the base URL of the endpoint (e.g., "/api/v1/aircrafts")
     * @param pageRequest the current page request
     * @param totalCount total number of records available
     * @param queryParams additional query parameters to include (e.g., filters, since)
     * @return navigation links with self, next, prev, first, and last
     * @deprecated Use {@link #createNavigationLinks(String, PageRequest, PaginationInfo)} instead
     */
    @Deprecated
    public static PageResponse.NavigationLinks buildNavigationLinks(
            String baseUrl, 
            PageRequest pageRequest, 
            long totalCount,
            Map<String, String> queryParams) {
        
        int limit = pageRequest.getLimit();
        int offset = pageRequest.getOffset();
        
        // Build parameter maps for each link
        Map<String, String> currentParams = new HashMap<>(queryParams);
        currentParams.put("limit", String.valueOf(limit));
        currentParams.put("offset", String.valueOf(offset));
        
        String self = buildUrl(baseUrl, currentParams);
        
        // Next link
        String next = null;
        if (pageRequest.hasNext(totalCount)) {
            Map<String, String> nextParams = new HashMap<>(queryParams);
            nextParams.put("limit", String.valueOf(limit));
            nextParams.put("offset", String.valueOf(offset + limit));
            next = buildUrl(baseUrl, nextParams);
        }
        
        // Previous link
        String prev = null;
        if (pageRequest.hasPrevious()) {
            Map<String, String> prevParams = new HashMap<>(queryParams);
            prevParams.put("limit", String.valueOf(limit));
            prevParams.put("offset", String.valueOf(Math.max(0, offset - limit)));
            prev = buildUrl(baseUrl, prevParams);
        }
        
        // First link
        Map<String, String> firstParams = new HashMap<>(queryParams);
        firstParams.put("limit", String.valueOf(limit));
        firstParams.put("offset", "0");
        String first = buildUrl(baseUrl, firstParams);
        
        // Last link
        Map<String, String> lastParams = new HashMap<>(queryParams);
        lastParams.put("limit", String.valueOf(limit));
        int lastOffset = calculateLastPageOffset(totalCount, limit);
        lastParams.put("offset", String.valueOf(lastOffset));
        String last = buildUrl(baseUrl, lastParams);
        
        return new PageResponse.NavigationLinks(self, next, prev, first, last);
    }
    
    /**
     * Builds HATEOAS navigation links without additional query parameters.
     * 
     * @param baseUrl the base URL of the endpoint
     * @param pageRequest the current page request
     * @param totalCount total number of records available
     * @return navigation links
     */
    public static PageResponse.NavigationLinks buildNavigationLinks(
            String baseUrl, 
            PageRequest pageRequest, 
            long totalCount) {
        return buildNavigationLinks(baseUrl, pageRequest, totalCount, new HashMap<>());
    }
    
    /**
     * Builds a complete URL with query parameters.
     * 
     * @param baseUrl the base URL
     * @param params query parameters to append
     * @return the complete URL with encoded parameters
     */
    public static String buildUrl(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        
        String queryString = params.entrySet().stream()
            .map(entry -> encodeParam(entry.getKey()) + "=" + encodeParam(entry.getValue()))
            .collect(Collectors.joining("&"));
        
        return baseUrl + "?" + queryString;
    }
    
    /**
     * URL encodes a parameter value.
     * 
     * @param value the value to encode
     * @return the URL-encoded value
     */
    public static String encodeParam(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    
    /**
     * Calculates the offset for the last page.
     * 
     * @param totalCount total number of records
     * @param limit records per page
     * @return offset for the last page
     */
    public static int calculateLastPageOffset(long totalCount, int limit) {
        if (totalCount == 0) {
            return 0;
        }
        long lastOffset = ((totalCount - 1) / limit) * limit;
        return (int) lastOffset;
    }
    
    /**
     * Calculates the total number of pages.
     * 
     * @param totalCount total number of records
     * @param limit records per page
     * @return total number of pages
     */
    public static int calculateTotalPages(long totalCount, int limit) {
        if (totalCount == 0 || limit <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / limit);
    }
    
    /**
     * Validates that offset is within bounds for the given total count and limit.
     * 
     * @param offset the offset to validate
     * @param totalCount total number of records
     * @param limit records per page
     * @return true if offset is valid
     */
    public static boolean isOffsetValid(int offset, long totalCount, int limit) {
        if (offset < 0) {
            return false;
        }
        if (totalCount == 0) {
            return offset == 0;
        }
        return offset < totalCount;
    }
    
    /**
     * Creates a PageRequest from limit and offset parameters.
     * Uses defaults if parameters are null or invalid.
     * 
     * @param limitParam the limit parameter (can be null)
     * @param offsetParam the offset parameter (can be null)
     * @return a valid PageRequest
     */
    public static PageRequest createPageRequest(Integer limitParam, Integer offsetParam) {
        int limit = (limitParam != null && limitParam > 0) 
            ? Math.min(limitParam, PageRequest.MAX_LIMIT) 
            : PageRequest.DEFAULT_LIMIT;
        
        int offset = (offsetParam != null && offsetParam >= 0) 
            ? offsetParam 
            : PageRequest.DEFAULT_OFFSET;
        
        return new PageRequest(limit, offset);
    }
    
    /**
     * Creates pagination info from query results.
     * 
     * @param totalCount total number of records matching the query
     * @param pageRequest the page request used
     * @return pagination metadata
     */
    public static PaginationInfo createPaginationInfo(
            long totalCount, 
            PageRequest pageRequest) {
        return new PaginationInfo(
            totalCount, 
            pageRequest.getLimit(), 
            pageRequest.getOffset()
        );
    }
    
    /**
     * Calculates the current page number (1-based) from offset and limit.
     * 
     * @param offset the current offset
     * @param limit records per page
     * @return the page number (1-based)
     */
    public static int calculatePageNumber(int offset, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        return (offset / limit) + 1;
    }
    
    /**
     * Calculates the starting record number for the current page (1-based).
     * 
     * @param offset the current offset
     * @return the starting record number
     */
    public static int calculateStartRecord(int offset) {
        return offset + 1;
    }
    
    /**
     * Calculates the ending record number for the current page (1-based).
     * 
     * @param offset the current offset
     * @param limit records per page
     * @param totalCount total number of records
     * @return the ending record number
     */
    public static int calculateEndRecord(int offset, int limit, long totalCount) {
        return (int) Math.min(offset + limit, totalCount);
    }
    
    /**
     * Creates a map of query parameters from common filter values.
     * Useful for building navigation links with filters preserved.
     * 
     * @param since delta sync timestamp (optional)
     * @param from range start timestamp (optional)
     * @param to range end timestamp (optional)
     * @param additionalParams other query parameters
     * @return map of query parameters
     */
    public static Map<String, String> createQueryParams(
            String since, 
            String from, 
            String to,
            Map<String, String> additionalParams) {
        
        Map<String, String> params = new HashMap<>();
        
        if (since != null && !since.isEmpty()) {
            params.put("since", since);
        }
        if (from != null && !from.isEmpty()) {
            params.put("from", from);
        }
        if (to != null && !to.isEmpty()) {
            params.put("to", to);
        }
        
        if (additionalParams != null) {
            params.putAll(additionalParams);
        }
        
        return params;
    }
}
