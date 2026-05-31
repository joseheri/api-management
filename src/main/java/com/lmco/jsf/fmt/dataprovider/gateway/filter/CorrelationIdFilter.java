package com.lmco.jsf.fmt.dataprovider.gateway.filter;

import com.lmco.jsf.fmt.dataprovider.common.correlation.CorrelationIdContext;
import com.lmco.jsf.fmt.dataprovider.common.correlation.CorrelationIdGenerator;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;

/**
 * JAX-RS filter for correlation ID management.
 * 
 * <p>This filter intercepts all HTTP requests and responses to:
 * <ul>
 *   <li>Extract correlation ID from X-Correlation-ID request header (if present)</li>
 *   <li>Generate new correlation ID if not provided by client</li>
 *   <li>Store correlation ID in thread-local context for request lifecycle</li>
 *   <li>Add correlation ID to X-Correlation-ID response header</li>
 *   <li>Log request start with correlation ID</li>
 *   <li>Clean up thread-local storage after request completion</li>
 * </ul>
 * 
 * <p><b>Priority:</b> Set to 1 to ensure this filter executes before other filters.
 * This guarantees correlation ID is available throughout the request processing.
 * 
 * <p><b>Header Name:</b> {@code X-Correlation-ID}
 * 
 * <p><b>Flow:</b>
 * <pre>
 * Client Request → Extract/Generate ID → Store in Context → Process Request
 *                                                                    ↓
 * Client Response ← Add to Headers ← Clean Context ← Complete Request
 * </pre>
 * 
 * <p><b>Example:</b>
 * <pre>
 * // Client sends request WITH correlation ID
 * GET /api/v1/aircrafts
 * X-Correlation-ID: client-trace-12345
 * 
 * // Server uses client's ID
 * Response:
 * HTTP/1.1 200 OK
 * X-Correlation-ID: client-trace-12345
 * 
 * // Client sends request WITHOUT correlation ID
 * GET /api/v1/aircrafts
 * 
 * // Server generates new ID
 * Response:
 * HTTP/1.1 200 OK
 * X-Correlation-ID: req-550e8400-e29b-41d4-a716-446655440000
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-21
 */
@Provider
@Priority(1)
public class CorrelationIdFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger LOG = Logger.getLogger(CorrelationIdFilter.class);
    
    /**
     * Standard header name for correlation ID.
     * Follows industry convention for distributed tracing.
     */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    
    /**
     * Intercepts incoming requests to extract or generate correlation ID.
     * 
     * <p>This method:
     * <ol>
     *   <li>Extracts correlation ID from request header (if present)</li>
     *   <li>Generates new correlation ID if not provided</li>
     *   <li>Stores ID in CorrelationIdContext for use throughout request</li>
     *   <li>Logs request start with correlation ID</li>
     * </ol>
     * 
     * @param requestContext the request context
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extract correlation ID from request header
        String correlationId = requestContext.getHeaderString(CORRELATION_ID_HEADER);
        
        // Generate new ID if not provided by client
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = CorrelationIdGenerator.generate();
            LOG.debugf("Generated new correlation ID: %s", correlationId);
        } else {
            LOG.debugf("Using client-provided correlation ID: %s", correlationId);
        }
        
        // Store in thread-local context for access throughout request processing
        CorrelationIdContext.set(correlationId);
        
        // Log request start with correlation ID for tracing
        LOG.infof("[%s] %s %s", 
            correlationId,
            requestContext.getMethod(),
            requestContext.getUriInfo().getPath()
        );
    }
    
    /**
     * Intercepts outgoing responses to add correlation ID and clean up.
     * 
     * <p>This method:
     * <ol>
     *   <li>Retrieves correlation ID from context</li>
     *   <li>Adds correlation ID to response header</li>
     *   <li>Clears thread-local storage to prevent memory leaks</li>
     * </ol>
     * 
     * @param requestContext the request context
     * @param responseContext the response context
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) 
            throws IOException {
        // Retrieve correlation ID from context
        String correlationId = CorrelationIdContext.get();
        
        // Add correlation ID to response header for client tracing
        if (correlationId != null) {
            responseContext.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
            
            // Log response with correlation ID and status code
            LOG.debugf("[%s] Response: %d", correlationId, responseContext.getStatus());
        }
        
        // Clean up thread-local storage to prevent memory leaks in thread pools
        CorrelationIdContext.clear();
    }
}
