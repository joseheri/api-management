package com.lmco.jsf.fmt.dataprovider.common.correlation;

/**
 * Thread-local storage for correlation IDs.
 * 
 * <p>Provides access to the current request's correlation ID throughout
 * the request lifecycle. This enables request tracing across multiple
 * service layers, logging, and error reporting.
 * 
 * <p><b>Usage:</b>
 * <pre>
 * // Set correlation ID (typically done by CorrelationIdFilter)
 * CorrelationIdContext.set("req-12345");
 * 
 * // Get correlation ID anywhere in request processing
 * String id = CorrelationIdContext.get();
 * 
 * // Clear after request (typically done by CorrelationIdFilter)
 * CorrelationIdContext.clear();
 * </pre>
 * 
 * <p><b>Thread Safety:</b> Uses ThreadLocal to ensure each thread
 * (request) has its own correlation ID without interference.
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-21
 */
public class CorrelationIdContext {
    
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private CorrelationIdContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Sets the correlation ID for the current thread/request.
     * 
     * @param id the correlation ID to set
     * @throws IllegalArgumentException if id is null or empty
     */
    public static void set(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Correlation ID cannot be null or empty");
        }
        correlationId.set(id);
    }
    
    /**
     * Gets the correlation ID for the current thread/request.
     * 
     * @return the correlation ID, or null if not set
     */
    public static String get() {
        return correlationId.get();
    }
    
    /**
     * Clears the correlation ID for the current thread/request.
     * 
     * <p>This should be called after request processing completes
     * to prevent memory leaks in thread pools.
     */
    public static void clear() {
        correlationId.remove();
    }
    
    /**
     * Checks if a correlation ID is set for the current thread.
     * 
     * @return true if correlation ID is set, false otherwise
     */
    public static boolean isSet() {
        return correlationId.get() != null;
    }
}
