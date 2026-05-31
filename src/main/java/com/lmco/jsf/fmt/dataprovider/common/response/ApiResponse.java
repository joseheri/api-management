package com.lmco.jsf.fmt.dataprovider.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lmco.jsf.fmt.dataprovider.common.pagination.NavigationLinks;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PaginationInfo;
import java.util.List;
import java.util.Objects;

/**
 * Standard API response wrapper for all endpoints.
 * 
 * <p>Provides consistent response structure with:
 * <ul>
 *   <li>Query metadata - information about the request parameters</li>
 *   <li>Data payload - the actual response data</li>
 *   <li>Pagination information - for paginated responses (optional)</li>
 *   <li>Navigation links - HATEOAS links for page navigation (optional)</li>
 * </ul>
 * 
 * <p>This wrapper ensures all API responses follow the same format,
 * making client integration easier and more predictable.
 * 
 * <p><b>Response Structure:</b>
 * <pre>
 * {
 *   "query": {
 *     "offset": 0,
 *     "limit": 10,
 *     "timestamp": "2026-05-22T10:00:00Z"
 *   },
 *   "data": [
 *     { "id": 1, "name": "Item 1" },
 *     { "id": 2, "name": "Item 2" }
 *   ],
 *   "pagination": {
 *     "totalCount": 42,
 *     "limit": 10,
 *     "offset": 0,
 *     "currentPage": 1,
 *     "totalPages": 5
 *   },
 *   "links": {
 *     "self": "?limit=10&offset=0",
 *     "next": "?limit=10&offset=10",
 *     "prev": null,
 *     "first": "?limit=10&offset=0",
 *     "last": "?limit=10&offset=40"
 *   }
 * }
 * </pre>
 * 
 * @param <T> the type of data in the response
 * @author MxSys Engineering Team
 * @version 2.0.0
 * @since 2026-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final QueryInfo query;
    private final List<T> data;
    private final PaginationInfo pagination;
    private final NavigationLinks links;
    
    /**
     * Creates a paginated API response with all components.
     * 
     * @param query query metadata (required)
     * @param data the response data (required)
     * @param pagination pagination information (optional, use null for non-paginated responses)
     * @param links navigation links (optional, use null if not applicable)
     */
    public ApiResponse(QueryInfo query, List<T> data, PaginationInfo pagination, NavigationLinks links) {
        this.query = Objects.requireNonNull(query, "Query info cannot be null");
        this.data = Objects.requireNonNull(data, "Data cannot be null");
        this.pagination = pagination;
        this.links = links;
    }
    
    /**
     * Creates a simple API response without pagination.
     * 
     * @param query query metadata (required)
     * @param data the response data (required)
     */
    public ApiResponse(QueryInfo query, List<T> data) {
        this(query, data, null, null);
    }
    
    /**
     * Creates an API response with data only (minimal constructor).
     * 
     * @param data the response data (required)
     */
    public ApiResponse(List<T> data) {
        this(QueryInfo.builder().build(), data, null, null);
    }
    
    /**
     * Gets the query metadata.
     * 
     * @return query information about the request
     */
    public QueryInfo getQuery() {
        return query;
    }
    
    /**
     * Gets the response data.
     * 
     * @return the data payload
     */
    public List<T> getData() {
        return data;
    }
    
    /**
     * Gets the pagination information.
     * 
     * @return pagination metadata, or null for non-paginated responses
     */
    public PaginationInfo getPagination() {
        return pagination;
    }
    
    /**
     * Gets the navigation links.
     * 
     * @return HATEOAS navigation links, or null if not applicable
     */
    public NavigationLinks getLinks() {
        return links;
    }
    
    /**
     * Creates an API response with query info and data.
     * 
     * @param <T> the data type
     * @param query the query metadata
     * @param data the response data
     * @return the API response
     */
    public static <T> ApiResponse<T> of(QueryInfo query, List<T> data) {
        return new ApiResponse<>(query, data);
    }
    
    /**
     * Creates a paginated API response.
     * 
     * @param <T> the data type
     * @param query the query metadata
     * @param data the response data
     * @param pagination the pagination info
     * @param links the navigation links
     * @return the API response
     */
    public static <T> ApiResponse<T> of(QueryInfo query, List<T> data, PaginationInfo pagination, NavigationLinks links) {
        return new ApiResponse<>(query, data, pagination, links);
    }
    
    /**
     * Creates a simple API response with data only.
     * 
     * @param <T> the data type
     * @param data the response data
     * @return the API response
     */
    public static <T> ApiResponse<T> of(List<T> data) {
        return new ApiResponse<>(data);
    }
    
    /**
     * Builder for ApiResponse.
     * 
     * @param <T> the data type
     */
    public static class Builder<T> {
        private QueryInfo query;
        private List<T> data;
        private PaginationInfo pagination;
        private NavigationLinks links;
        
        /**
         * Sets the query metadata.
         */
        public Builder<T> query(QueryInfo query) {
            this.query = query;
            return this;
        }
        
        /**
         * Sets the response data.
         */
        public Builder<T> data(List<T> data) {
            this.data = data;
            return this;
        }
        
        /**
         * Sets the pagination information.
         */
        public Builder<T> pagination(PaginationInfo pagination) {
            this.pagination = pagination;
            return this;
        }
        
        /**
         * Sets the navigation links.
         */
        public Builder<T> links(NavigationLinks links) {
            this.links = links;
            return this;
        }
        
        /**
         * Builds the ApiResponse.
         * 
         * @return the API response
         * @throws NullPointerException if query or data is null
         */
        public ApiResponse<T> build() {
            return new ApiResponse<>(query, data, pagination, links);
        }
    }
    
    /**
     * Creates a new builder.
     * 
     * @param <T> the data type
     * @return a new builder instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiResponse<?> that = (ApiResponse<?>) o;
        return Objects.equals(query, that.query) &&
               Objects.equals(data, that.data) &&
               Objects.equals(pagination, that.pagination) &&
               Objects.equals(links, that.links);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(query, data, pagination, links);
    }
    
    @Override
    public String toString() {
        return String.format(
            "ApiResponse{query=%s, dataSize=%d, pagination=%s, links=%s}",
            query, data != null ? data.size() : 0, pagination, links
        );
    }
}
