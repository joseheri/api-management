package com.lmco.jsf.fmt.dataprovider.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lmco.jsf.fmt.dataprovider.common.filtering.TimeFilter;
import com.lmco.jsf.fmt.dataprovider.common.pagination.PageRequest;
import java.time.Instant;
import java.util.Objects;

/**
 * Query metadata included in API responses.
 * 
 * <p>Provides information about the query parameters used to generate the response,
 * helping clients understand what filters and pagination were applied.
 * 
 * <p><b>Response Structure:</b>
 * <pre>
 * {
 *   "query": {
 *     "offset": 0,
 *     "limit": 100,
 *     "since": "2026-05-01T00:00:00Z",
 *     "timestamp": "2026-05-20T22:30:00Z"
 *   },
 *   "data": [ ... ]
 * }
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryInfo {
    
    private final Integer offset;
    private final Integer limit;
    private final Instant since;
    private final Instant from;
    private final Instant to;
    private final Instant timestamp;
    
    /**
     * Private constructor - use builder.
     */
    private QueryInfo(Integer offset, Integer limit, Instant since, Instant from, 
                     Instant to, Instant timestamp) {
        this.offset = offset;
        this.limit = limit;
        this.since = since;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }
    
    /**
     * Gets the pagination offset.
     * 
     * @return the offset, or null if not applicable
     */
    public Integer getOffset() {
        return offset;
    }
    
    /**
     * Gets the pagination limit (page size).
     * 
     * @return the limit, or null if not applicable
     */
    public Integer getLimit() {
        return limit;
    }
    
    /**
     * Gets the 'since' timestamp for delta sync queries.
     * 
     * @return the since timestamp, or null if not used
     */
    public Instant getSince() {
        return since;
    }
    
    /**
     * Gets the 'from' timestamp for range queries.
     * 
     * @return the from timestamp, or null if not used
     */
    public Instant getFrom() {
        return from;
    }
    
    /**
     * Gets the 'to' timestamp for range queries.
     * 
     * @return the to timestamp, or null if not used
     */
    public Instant getTo() {
        return to;
    }
    
    /**
     * Gets the timestamp when the query was executed.
     * 
     * @return the query execution timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Creates a QueryInfo from pagination and time filter.
     * 
     * @param pageRequest the pagination parameters (can be null)
     * @param timeFilter the time filter parameters (can be null)
     * @return the query info
     */
    public static QueryInfo from(PageRequest pageRequest, TimeFilter timeFilter) {
        Builder builder = builder();
        
        if (pageRequest != null) {
            builder.offset(pageRequest.getOffset())
                   .limit(pageRequest.getLimit());
        }
        
        if (timeFilter != null && !timeFilter.isEmpty()) {
            if (timeFilter.isDelta()) {
                builder.since(timeFilter.getSince());
            } else if (timeFilter.isRange()) {
                builder.from(timeFilter.getFrom())
                       .to(timeFilter.getTo());
            }
        }
        
        return builder.build();
    }
    
    /**
     * Builder for QueryInfo.
     */
    public static class Builder {
        private Integer offset;
        private Integer limit;
        private Instant since;
        private Instant from;
        private Instant to;
        private Instant timestamp;
        
        /**
         * Sets the pagination offset.
         */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }
        
        /**
         * Sets the pagination limit.
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }
        
        /**
         * Sets the delta sync 'since' timestamp.
         */
        public Builder since(Instant since) {
            this.since = since;
            return this;
        }
        
        /**
         * Sets the range 'from' timestamp.
         */
        public Builder from(Instant from) {
            this.from = from;
            return this;
        }
        
        /**
         * Sets the range 'to' timestamp.
         */
        public Builder to(Instant to) {
            this.to = to;
            return this;
        }
        
        /**
         * Sets the query execution timestamp.
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        /**
         * Builds the QueryInfo.
         */
        public QueryInfo build() {
            return new QueryInfo(offset, limit, since, from, to, timestamp);
        }
    }
    
    /**
     * Creates a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryInfo queryInfo = (QueryInfo) o;
        return Objects.equals(offset, queryInfo.offset) &&
               Objects.equals(limit, queryInfo.limit) &&
               Objects.equals(since, queryInfo.since) &&
               Objects.equals(from, queryInfo.from) &&
               Objects.equals(to, queryInfo.to) &&
               Objects.equals(timestamp, queryInfo.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, since, from, to, timestamp);
    }
    
    @Override
    public String toString() {
        return String.format(
            "QueryInfo{offset=%d, limit=%d, since=%s, from=%s, to=%s, timestamp=%s}",
            offset, limit, since, from, to, timestamp
        );
    }
}
