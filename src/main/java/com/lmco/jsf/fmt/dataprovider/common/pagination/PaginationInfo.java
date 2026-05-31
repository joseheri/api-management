package com.lmco.jsf.fmt.dataprovider.common.pagination;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Pagination metadata for API responses.
 * 
 * <p>Provides information about the current page, total records,
 * and navigation state for paginated results.
 * 
 * <p>Used in {@link com.lmco.jsf.fmt.dataprovider.common.response.ApiResponse} to provide
 * pagination context to API consumers.
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationInfo {
    
    private final long totalCount;
    private final int limit;
    private final int offset;
    private final int currentPage;
    private final int totalPages;
    
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
        this.currentPage = calculateCurrentPage(offset, limit);
        this.totalPages = calculateTotalPages(totalCount, limit);
    }
    
    /**
     * Gets the total number of records.
     * 
     * @return total count of records
     */
    public long getTotalCount() {
        return totalCount;
    }
    
    /**
     * Gets the page size limit.
     * 
     * @return maximum records per page
     */
    public int getLimit() {
        return limit;
    }
    
    /**
     * Gets the offset.
     * 
     * @return number of records skipped
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Gets the current page number (1-based).
     * 
     * @return the current page number
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Gets the total number of pages.
     * 
     * @return total page count
     */
    public int getTotalPages() {
        return totalPages;
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
    
    /**
     * Calculates the current page number (1-based).
     * 
     * @param offset the offset
     * @param limit the page size
     * @return the page number
     */
    private static int calculateCurrentPage(int offset, int limit) {
        if (limit <= 0) {
            return 1;
        }
        return (offset / limit) + 1;
    }
    
    /**
     * Calculates the total number of pages.
     * 
     * @param totalCount total records
     * @param limit page size
     * @return total pages
     */
    private static int calculateTotalPages(long totalCount, int limit) {
        if (limit <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalCount / limit);
    }
    
    @Override
    public String toString() {
        return String.format("PaginationInfo{totalCount=%d, limit=%d, offset=%d, page=%d/%d}", 
            totalCount, limit, offset, currentPage, totalPages);
    }
}
