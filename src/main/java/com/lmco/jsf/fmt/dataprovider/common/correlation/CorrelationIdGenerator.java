package com.lmco.jsf.fmt.dataprovider.common.correlation;

import java.util.UUID;

/**
 * Generates unique correlation IDs for request tracing.
 * 
 * <p>Creates unique identifiers in the format "req-{uuid}" for tracking
 * requests across the system. These IDs are used for logging, error
 * reporting, and distributed tracing.
 * 
 * <p><b>Format:</b> {@code req-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}
 * 
 * <p><b>Example:</b> {@code req-550e8400-e29b-41d4-a716-446655440000}
 * 
 * <p><b>Usage:</b>
 * <pre>
 * String correlationId = CorrelationIdGenerator.generate();
 * // Returns: "req-550e8400-e29b-41d4-a716-446655440000"
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-21
 */
public class CorrelationIdGenerator {
    
    /**
     * Prefix for all generated correlation IDs.
     */
    private static final String CORRELATION_ID_PREFIX = "req-";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private CorrelationIdGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Generates a new unique correlation ID.
     * 
     * <p>Uses UUID v4 (random) to ensure uniqueness across distributed systems.
     * The format is "req-" followed by a standard UUID string.
     * 
     * @return a new unique correlation ID in format "req-{uuid}"
     */
    public static String generate() {
        return CORRELATION_ID_PREFIX + UUID.randomUUID().toString();
    }
    
    /**
     * Validates if a string is a valid correlation ID format.
     * 
     * <p>Checks if the ID starts with "req-" and follows UUID format.
     * 
     * @param id the ID to validate
     * @return true if valid correlation ID format, false otherwise
     */
    public static boolean isValid(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        // Must start with prefix
        if (!id.startsWith(CORRELATION_ID_PREFIX)) {
            return false;
        }
        
        // Extract UUID part and validate
        String uuidPart = id.substring(CORRELATION_ID_PREFIX.length());
        try {
            UUID.fromString(uuidPart);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
