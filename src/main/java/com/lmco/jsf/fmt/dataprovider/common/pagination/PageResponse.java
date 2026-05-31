package com.lmco.jsf.fmt.dataprovider.common.pagination;

import java.util.List;
import java.util.Objects;

/**
 * Generic paginated response wrapper for API endpoints.
 * 
 * <p>Encapsulates the actual data along with pagination metadata including
 * total count, current page parameters, and HATEOAS navigation links.
 * 
 * <p>Response structure matches the OpenAPI specification:
 * <pre>
 * {
 *   "data": [...],
 *   "pagination": {
 *     "totalCount": 42,
 *     "limit": 10,
 *     "offset": 0
 *   },
 *   "links": {
 *     "self": "/api/v1/resource?limit=10&offset=0",
 *     "next": "/api/v1/resource?limit=10&offset=10",
 *     "prev": null,
 *     "first": "/api/v1/resource?limit=10&offset=0",
 *     "last": "/api/v1/resource?limit=10&offset=30"
 *   }
 * }
 * </pre>
 * 
 * @param <T> the type of data items in the response
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public class PageResponse<T> {
    
    private final List<T> data;
    private final PaginationInfo pagination;
    private final NavigationLinks links;
    
    /**
     * Creates a paginated response.
     * 
     * @param data the list of data items for this page
     * @param pagination pagination metadata
     * @param links navigation links for HATEOAS
     */
    public PageResponse(List<T> data, PaginationInfo pagination, NavigationLinks links) {
        this.data = Objects.requireNonNull(data, "data cannot be null");
        this.pagination = Objects.requireNonNull(pagination, "pagination cannot be null");
        this.links = links; // Can be null if links not needed
    }
    
    /**
     * Creates a paginated response without navigation links.
     * 
     * @param data the list of data items for this page
     * @param pagination pagination metadata
     */
    public PageResponse(List<T> data, PaginationInfo pagination) {
        this(data, pagination, null);
    }
    
    /**
     * Gets the data items for this page.
     * 
     * @return the list of data items
     */
    public List<T> getData() {
        return data;
    }
    
    /**
     * Gets the pagination metadata.
     * 
     * @return pagination information
     */
    public PaginationInfo getPagination() {
        return pagination;
    }
    
    /**
     * Gets the navigation links.
     * 
     * @return HATEOAS navigation links, or null if not provided
     */
    public NavigationLinks getLinks() {
        return links;
    }
    
    /**
     * Checks if this page is empty.
     * 
     * @return true if data list is empty
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    /**
     * Gets the number of items in this page.
     * 
     * @return the size of the data list
     */
    public int getSize() {
        return data.size();
    }
    
    /**
     * Checks if there is a next page available.
     * 
     * @return true if a next page exists
     */
    public boolean hasNext() {
        return pagination.hasNext();
    }
    
    /**
     * Checks if there is a previous page available.
     * 
     * @return true if a previous page exists
     */
    public boolean hasPrevious() {
        return pagination.hasPrevious();
    }
    
    @Override
    public String toString() {
        return String.format("PageResponse{items=%d, totalCount=%d, limit=%d, offset=%d}", 
            data.size(), pagination.getTotalCount(), pagination.getLimit(), pagination.getOffset());
    }
    
    /**
     * Pagination metadata.
     * Included in all paginated responses.
     */
    public static class PaginationInfo {
        private final long totalCount;
        private final int limit;
        private final int offset;
        
        /**
         * Creates pagination metadata.
         * 
         * @param totalCount total number of records matching the query
         * @param limit maximum number of records per page
         * @param offset number of records skipped
         */
        public PaginationInfo(long totalCount, int limit, int offset) {
            this.totalCount = totalCount;
            this.limit = limit;
            this.offset = offset;
        }
        
        public long getTotalCount() {
            return totalCount;
        }
        
        public int getLimit() {
            return limit;
        }
        
        public int getOffset() {
            return offset;
        }
        
        /**
         * Calculates the current page number (1-based).
         * 
         * @return the page number
         */
        public int getCurrentPage() {
            return (offset / limit) + 1;
        }
        
        /**
         * Calculates the total number of pages.
         * 
         * @return the total page count
         */
        public int getTotalPages() {
            return (int) Math.ceil((double) totalCount / limit);
        }
        
        /**
         * Checks if there is a next page.
         * 
         * @return true if more records exist after current page
         */
        public boolean hasNext() {
            return (offset + limit) < totalCount;
        }
        
        /**
         * Checks if there is a previous page.
         * 
         * @return true if offset is greater than 0
         */
        public boolean hasPrevious() {
            return offset > 0;
        }
        
        @Override
        public String toString() {
            return String.format("PaginationInfo{totalCount=%d, limit=%d, offset=%d, page=%d/%d}", 
                totalCount, limit, offset, getCurrentPage(), getTotalPages());
        }
    }
    
    /**
     * HATEOAS navigation links for pagination.
     * Provides hypermedia controls for navigating between pages.
     */
    public static class NavigationLinks {
        private final String self;
        private final String next;
        private final String prev;
        private final String first;
        private final String last;
        
        /**
         * Creates navigation links.
         * 
         * @param self link to current page
         * @param next link to next page (null if none)
         * @param prev link to previous page (null if none)
         * @param first link to first page
         * @param last link to last page
         */
        public NavigationLinks(String self, String next, String prev, String first, String last) {
            this.self = self;
            this.next = next;
            this.prev = prev;
            this.first = first;
            this.last = last;
        }
        
        public String getSelf() {
            return self;
        }
        
        public String getNext() {
            return next;
        }
        
        public String getPrev() {
            return prev;
        }
        
        public String getFirst() {
            return first;
        }
        
        public String getLast() {
            return last;
        }
        
        @Override
        public String toString() {
            return String.format("NavigationLinks{self='%s', next='%s', prev='%s', first='%s', last='%s'}", 
                self, next, prev, first, last);
        }
    }
}
