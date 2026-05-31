package com.lmco.jsf.fmt.dataprovider.common.pagination;

/**
 * Represents pagination parameters for API requests.
 * 
 * <p>Encapsulates limit (page size) and offset (starting position) parameters
 * used for paginating query results. Provides validation to ensure parameters
 * are within acceptable ranges.
 * 
 * <p>Default values:
 * <ul>
 *   <li>Limit: 100 records per page</li>
 *   <li>Offset: 0 (start from beginning)</li>
 * </ul>
 * 
 * <p>Constraints:
 * <ul>
 *   <li>Limit: 1 to 1000 records</li>
 *   <li>Offset: 0 or greater</li>
 * </ul>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public class PageRequest {
    
    /** Default number of records per page */
    public static final int DEFAULT_LIMIT = 100;
    
    /** Minimum allowed page size */
    public static final int MIN_LIMIT = 1;
    
    /** Maximum allowed page size */
    public static final int MAX_LIMIT = 1000;
    
    /** Default offset (start from beginning) */
    public static final int DEFAULT_OFFSET = 0;
    
    /** Minimum allowed offset */
    public static final int MIN_OFFSET = 0;
    
    private final int limit;
    private final int offset;
    
    /**
     * Creates a page request with default values.
     * Uses limit=100 and offset=0.
     */
    public PageRequest() {
        this(DEFAULT_LIMIT, DEFAULT_OFFSET);
    }
    
    /**
     * Creates a page request with specified limit and offset.
     * 
     * @param limit the maximum number of records to return (1-1000)
     * @param offset the number of records to skip (0 or greater)
     * @throws IllegalArgumentException if parameters are out of valid range
     */
    public PageRequest(int limit, int offset) {
        validateLimit(limit);
        validateOffset(offset);
        this.limit = limit;
        this.offset = offset;
    }
    
    /**
     * Creates a page request with only limit specified.
     * Uses offset=0.
     * 
     * @param limit the maximum number of records to return (1-1000)
     * @throws IllegalArgumentException if limit is out of valid range
     */
    public PageRequest(int limit) {
        this(limit, DEFAULT_OFFSET);
    }
    
    /**
     * Gets the maximum number of records to return.
     * 
     * @return the page size limit
     */
    public int getLimit() {
        return limit;
    }
    
    /**
     * Gets the number of records to skip.
     * 
     * @return the offset value
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Calculates the page number based on offset and limit.
     * Page numbers are 1-based.
     * 
     * @return the current page number
     */
    public int getPageNumber() {
        return (offset / limit) + 1;
    }
    
    /**
     * Creates a PageRequest for the next page.
     * 
     * @return a new PageRequest with incremented offset
     */
    public PageRequest next() {
        return new PageRequest(limit, offset + limit);
    }
    
    /**
     * Creates a PageRequest for the previous page.
     * Returns current page if already at first page.
     * 
     * @return a new PageRequest with decremented offset, or current if at start
     */
    public PageRequest previous() {
        if (offset == 0) {
            return this;
        }
        int newOffset = Math.max(0, offset - limit);
        return new PageRequest(limit, newOffset);
    }
    
    /**
     * Checks if there might be a next page.
     * This is a hint based on whether we're at the start; actual availability
     * depends on total count.
     * 
     * @param totalCount the total number of records available
     * @return true if there are more records after current page
     */
    public boolean hasNext(long totalCount) {
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
    
    /**
     * Validates the limit parameter.
     * 
     * @param limit the limit to validate
     * @throws IllegalArgumentException if limit is out of valid range
     */
    private void validateLimit(int limit) {
        if (limit < MIN_LIMIT) {
            throw new IllegalArgumentException(
                String.format("Limit must be at least %d, got: %d", MIN_LIMIT, limit)
            );
        }
        if (limit > MAX_LIMIT) {
            throw new IllegalArgumentException(
                String.format("Limit must be at most %d, got: %d", MAX_LIMIT, limit)
            );
        }
    }
    
    /**
     * Validates the offset parameter.
     * 
     * @param offset the offset to validate
     * @throws IllegalArgumentException if offset is negative
     */
    private void validateOffset(int offset) {
        if (offset < MIN_OFFSET) {
            throw new IllegalArgumentException(
                String.format("Offset must be at least %d, got: %d", MIN_OFFSET, offset)
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format("PageRequest{limit=%d, offset=%d, page=%d}", 
            limit, offset, getPageNumber());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRequest that = (PageRequest) o;
        return limit == that.limit && offset == that.offset;
    }
    
    @Override
    public int hashCode() {
        int result = limit;
        result = 31 * result + offset;
        return result;
    }
}
