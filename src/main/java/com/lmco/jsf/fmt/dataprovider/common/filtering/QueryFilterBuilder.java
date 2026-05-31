package com.lmco.jsf.fmt.dataprovider.common.filtering;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds JPA criteria predicates for time-based filtering.
 * 
 * <p>Converts TimeFilter objects into JPA Criteria API predicates that can be
 * applied to database queries. Supports filtering on a timestamp field using
 * delta sync (since) or time range (from/to) patterns.
 * 
 * <p><b>Usage Example:</b>
 * <pre>
 * // In a repository query method
 * CriteriaBuilder cb = entityManager.getCriteriaBuilder();
 * CriteriaQuery&lt;WorkOrder&gt; query = cb.createQuery(WorkOrder.class);
 * Root&lt;WorkOrder&gt; root = query.from(WorkOrder.class);
 * 
 * // Build time filter predicate
 * TimeFilter timeFilter = TimeFilter.since(Instant.parse("2026-05-01T00:00:00Z"));
 * Predicate timePredicate = QueryFilterBuilder.buildTimePredicate(
 *     cb, 
 *     root.get("lastModifiedTimestamp"), 
 *     timeFilter
 * );
 * 
 * // Apply to query
 * query.where(timePredicate);
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public final class QueryFilterBuilder {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private QueryFilterBuilder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Builds a JPA predicate for time-based filtering.
     * 
     * <p>Creates predicates based on the TimeFilter type:
     * <ul>
     *   <li><b>DELTA</b>: {@code field >= since}</li>
     *   <li><b>RANGE (closed)</b>: {@code field >= from AND field <= to}</li>
     *   <li><b>RANGE (from only)</b>: {@code field >= from}</li>
     *   <li><b>RANGE (to only)</b>: {@code field <= to}</li>
     *   <li><b>NONE</b>: No predicate (returns null)</li>
     * </ul>
     * 
     * @param cb the criteria builder
     * @param timestampField the entity field to filter on (e.g., lastModifiedTimestamp)
     * @param timeFilter the time filter to apply
     * @return a predicate for the time filter, or null if no filtering
     */
    public static Predicate buildTimePredicate(
            CriteriaBuilder cb,
            Expression<Instant> timestampField,
            TimeFilter timeFilter) {
        
        if (timeFilter == null || timeFilter.isEmpty()) {
            return null; // No filtering
        }
        
        switch (timeFilter.getType()) {
            case DELTA:
                return buildDeltaPredicate(cb, timestampField, timeFilter.getSince());
                
            case RANGE:
                return buildRangePredicate(cb, timestampField, timeFilter.getFrom(), timeFilter.getTo());
                
            case NONE:
            default:
                return null;
        }
    }
    
    /**
     * Builds a delta sync predicate (since parameter).
     * Creates: {@code field >= since}
     * 
     * @param cb the criteria builder
     * @param timestampField the timestamp field to filter
     * @param since the since timestamp (inclusive)
     * @return predicate for records modified on or after the since timestamp
     */
    public static Predicate buildDeltaPredicate(
            CriteriaBuilder cb,
            Expression<Instant> timestampField,
            Instant since) {
        
        if (since == null) {
            return null;
        }
        
        // field >= since (inclusive)
        return cb.greaterThanOrEqualTo(timestampField, since);
    }
    
    /**
     * Builds a time range predicate (from/to parameters).
     * Creates predicates based on which boundaries are provided:
     * <ul>
     *   <li>Both: {@code field >= from AND field <= to}</li>
     *   <li>From only: {@code field >= from}</li>
     *   <li>To only: {@code field <= to}</li>
     * </ul>
     * 
     * @param cb the criteria builder
     * @param timestampField the timestamp field to filter
     * @param from the from timestamp (inclusive, can be null)
     * @param to the to timestamp (inclusive, can be null)
     * @return predicate for the time range
     */
    public static Predicate buildRangePredicate(
            CriteriaBuilder cb,
            Expression<Instant> timestampField,
            Instant from,
            Instant to) {
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (from != null) {
            // field >= from (inclusive)
            predicates.add(cb.greaterThanOrEqualTo(timestampField, from));
        }
        
        if (to != null) {
            // field <= to (inclusive)
            predicates.add(cb.lessThanOrEqualTo(timestampField, to));
        }
        
        if (predicates.isEmpty()) {
            return null;
        }
        
        if (predicates.size() == 1) {
            return predicates.get(0);
        }
        
        // Combine with AND
        return cb.and(predicates.toArray(new Predicate[0]));
    }
    
    /**
     * Builds a combined predicate from multiple conditions.
     * Combines all non-null predicates with AND logic.
     * 
     * @param cb the criteria builder
     * @param predicates varargs of predicates (nulls are ignored)
     * @return combined predicate, or null if all predicates are null
     */
    public static Predicate combinePredicates(CriteriaBuilder cb, Predicate... predicates) {
        List<Predicate> nonNullPredicates = new ArrayList<>();
        
        for (Predicate p : predicates) {
            if (p != null) {
                nonNullPredicates.add(p);
            }
        }
        
        if (nonNullPredicates.isEmpty()) {
            return null;
        }
        
        if (nonNullPredicates.size() == 1) {
            return nonNullPredicates.get(0);
        }
        
        return cb.and(nonNullPredicates.toArray(new Predicate[0]));
    }
    
    /**
     * Builds a predicate for string field equality (case-sensitive).
     * 
     * @param cb the criteria builder
     * @param field the string field to filter
     * @param value the value to match
     * @return equality predicate, or null if value is null/empty
     */
    public static Predicate buildStringEquals(
            CriteriaBuilder cb,
            Expression<String> field,
            String value) {
        
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        return cb.equal(field, value);
    }
    
    /**
     * Builds a predicate for string field equality (case-insensitive).
     * 
     * @param cb the criteria builder
     * @param field the string field to filter
     * @param value the value to match
     * @return case-insensitive equality predicate, or null if value is null/empty
     */
    public static Predicate buildStringEqualsIgnoreCase(
            CriteriaBuilder cb,
            Expression<String> field,
            String value) {
        
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        return cb.equal(cb.lower(field), value.toLowerCase());
    }
    
    /**
     * Builds a predicate for string field LIKE matching (case-insensitive).
     * Adds wildcards for partial matching.
     * 
     * @param cb the criteria builder
     * @param field the string field to filter
     * @param pattern the pattern to match (wildcards added automatically)
     * @return LIKE predicate, or null if pattern is null/empty
     */
    public static Predicate buildStringLike(
            CriteriaBuilder cb,
            Expression<String> field,
            String pattern) {
        
        if (pattern == null || pattern.isEmpty()) {
            return null;
        }
        
        String likePattern = "%" + pattern.toLowerCase() + "%";
        return cb.like(cb.lower(field), likePattern);
    }
    
    /**
     * Builds a predicate for enum field equality.
     * 
     * @param <E> the enum type
     * @param cb the criteria builder
     * @param field the enum field to filter
     * @param value the enum value to match
     * @return equality predicate, or null if value is null
     */
    public static <E extends Enum<E>> Predicate buildEnumEquals(
            CriteriaBuilder cb,
            Expression<E> field,
            E value) {
        
        if (value == null) {
            return null;
        }
        
        return cb.equal(field, value);
    }
    
    /**
     * Builds a predicate for boolean field equality.
     * 
     * @param cb the criteria builder
     * @param field the boolean field to filter
     * @param value the boolean value to match
     * @return equality predicate, or null if value is null
     */
    public static Predicate buildBooleanEquals(
            CriteriaBuilder cb,
            Expression<Boolean> field,
            Boolean value) {
        
        if (value == null) {
            return null;
        }
        
        return value ? cb.isTrue(field) : cb.isFalse(field);
    }
    
    /**
     * Builds a predicate for IN clause (field matches any value in list).
     * 
     * @param <T> the field type
     * @param field the field to filter
     * @param values the list of values to match
     * @return IN predicate, or null if values is null/empty
     */
    public static <T> Predicate buildInClause(Expression<T> field, List<T> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        
        return field.in(values);
    }
}
