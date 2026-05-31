package com.lmco.jsf.fmt.dataprovider.common.filtering;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents time-based filtering parameters for API queries.
 * 
 * <p>Supports three filtering modes:
 * <ul>
 *   <li><b>Delta Sync</b>: Records modified after a specific timestamp (since)</li>
 *   <li><b>Time Range</b>: Records modified within a time window (from/to)</li>
 *   <li><b>No Filter</b>: All records regardless of modification time</li>
 * </ul>
 * 
 * <p><b>Valid Parameter Combinations:</b>
 * <ul>
 *   <li>{@code since} alone - Delta sync (incremental updates)</li>
 *   <li>{@code from} and {@code to} - Time range query</li>
 *   <li>{@code from} alone - Open-ended from timestamp</li>
 *   <li>{@code to} alone - Open-ended up to timestamp</li>
 *   <li>None - No time filtering</li>
 * </ul>
 * 
 * <p><b>Invalid Combination:</b>
 * <ul>
 *   <li>{@code since} with {@code from} or {@code to} - Conflicting filter types</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>
 * // Delta sync - get records modified after May 1st
 * TimeFilter deltaFilter = TimeFilter.since(Instant.parse("2026-05-01T00:00:00Z"));
 * 
 * // Time range - get records modified in April
 * TimeFilter rangeFilter = TimeFilter.range(
 *     Instant.parse("2026-04-01T00:00:00Z"),
 *     Instant.parse("2026-04-30T23:59:59Z")
 * );
 * 
 * // No filter - get all records
 * TimeFilter noFilter = TimeFilter.empty();
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public class TimeFilter {
    
    /**
     * Type of time filter applied.
     */
    public enum FilterType {
        /** Delta sync using 'since' parameter (incremental updates) */
        DELTA,
        
        /** Time range using 'from' and/or 'to' parameters */
        RANGE,
        
        /** No time filtering applied */
        NONE
    }
    
    private final FilterType type;
    private final Instant since;
    private final Instant from;
    private final Instant to;
    
    /**
     * Private constructor - use static factory methods instead.
     */
    private TimeFilter(FilterType type, Instant since, Instant from, Instant to) {
        this.type = Objects.requireNonNull(type, "Filter type cannot be null");
        this.since = since;
        this.from = from;
        this.to = to;
    }
    
    /**
     * Creates a delta sync filter (since parameter).
     * For incremental updates - returns records modified after the specified timestamp.
     * 
     * @param since the timestamp to filter from (inclusive)
     * @return a delta sync time filter
     * @throws NullPointerException if since is null
     */
    public static TimeFilter since(Instant since) {
        Objects.requireNonNull(since, "Since timestamp cannot be null");
        return new TimeFilter(FilterType.DELTA, since, null, null);
    }
    
    /**
     * Creates a time range filter (from/to parameters).
     * Returns records modified within the specified time window.
     * 
     * @param from the start of the time range (inclusive, can be null for open-ended)
     * @param to the end of the time range (inclusive, can be null for open-ended)
     * @return a time range filter
     * @throws IllegalArgumentException if both from and to are null
     */
    public static TimeFilter range(Instant from, Instant to) {
        if (from == null && to == null) {
            throw new IllegalArgumentException(
                "At least one of 'from' or 'to' must be specified for a range filter"
            );
        }
        return new TimeFilter(FilterType.RANGE, null, from, to);
    }
    
    /**
     * Creates an empty filter (no time filtering).
     * Returns all records regardless of modification time.
     * 
     * @return an empty time filter
     */
    public static TimeFilter empty() {
        return new TimeFilter(FilterType.NONE, null, null, null);
    }
    
    /**
     * Creates a time filter from optional parameters.
     * Automatically determines the filter type based on which parameters are provided.
     * 
     * @param since delta sync timestamp (optional)
     * @param from range start timestamp (optional)
     * @param to range end timestamp (optional)
     * @return a time filter matching the provided parameters
     * @throws IllegalArgumentException if invalid parameter combination
     */
    public static TimeFilter from(Instant since, Instant from, Instant to) {
        // Validate that since is not used with from/to
        if (since != null && (from != null || to != null)) {
            throw new IllegalArgumentException(
                "Cannot use 'since' parameter with 'from' or 'to'. " +
                "Use either delta sync (since) or time range (from/to)."
            );
        }
        
        if (since != null) {
            return since(since);
        }
        
        if (from != null || to != null) {
            return range(from, to);
        }
        
        return empty();
    }
    
    /**
     * Gets the filter type.
     * 
     * @return the type of time filter
     */
    public FilterType getType() {
        return type;
    }
    
    /**
     * Gets the 'since' timestamp for delta sync.
     * 
     * @return the since timestamp, or null if not a delta filter
     */
    public Instant getSince() {
        return since;
    }
    
    /**
     * Gets the 'from' timestamp for range filter.
     * 
     * @return the from timestamp, or null if not specified
     */
    public Instant getFrom() {
        return from;
    }
    
    /**
     * Gets the 'to' timestamp for range filter.
     * 
     * @return the to timestamp, or null if not specified
     */
    public Instant getTo() {
        return to;
    }
    
    /**
     * Checks if this is a delta sync filter.
     * 
     * @return true if filter type is DELTA
     */
    public boolean isDelta() {
        return type == FilterType.DELTA;
    }
    
    /**
     * Checks if this is a time range filter.
     * 
     * @return true if filter type is RANGE
     */
    public boolean isRange() {
        return type == FilterType.RANGE;
    }
    
    /**
     * Checks if this is an empty filter (no filtering).
     * 
     * @return true if filter type is NONE
     */
    public boolean isEmpty() {
        return type == FilterType.NONE;
    }
    
    /**
     * Checks if the range has both from and to timestamps.
     * 
     * @return true if this is a closed range with both boundaries
     */
    public boolean isClosedRange() {
        return isRange() && from != null && to != null;
    }
    
    /**
     * Checks if the range is open-ended (missing from or to).
     * 
     * @return true if this is a range with only one boundary
     */
    public boolean isOpenRange() {
        return isRange() && (from == null || to == null);
    }
    
    /**
     * Gets a description of the filter for logging/display.
     * 
     * @return a human-readable description of the filter
     */
    public String getDescription() {
        switch (type) {
            case DELTA:
                return String.format("Delta sync since %s", since);
            case RANGE:
                if (from != null && to != null) {
                    return String.format("Range from %s to %s", from, to);
                } else if (from != null) {
                    return String.format("From %s (open-ended)", from);
                } else {
                    return String.format("Until %s (open-ended)", to);
                }
            case NONE:
            default:
                return "No time filter";
        }
    }
    
    @Override
    public String toString() {
        return String.format("TimeFilter{type=%s, since=%s, from=%s, to=%s}", 
            type, since, from, to);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeFilter that = (TimeFilter) o;
        return type == that.type &&
               Objects.equals(since, that.since) &&
               Objects.equals(from, that.from) &&
               Objects.equals(to, that.to);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, since, from, to);
    }
}
