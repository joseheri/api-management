package com.lmco.jsf.fmt.dataprovider.common.error;

import com.lmco.jsf.fmt.dataprovider.common.correlation.CorrelationIdContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Global exception mapper for the API.
 * 
 * <p>Catches all unhandled exceptions and converts them to standardized
 * ErrorResponse format. Provides consistent error handling across all endpoints.
 * 
 * <p>Handles:
 * <ul>
 *   <li>WebApplicationException (JAX-RS exceptions with status codes)</li>
 *   <li>IllegalArgumentException (validation errors → 400)</li>
 *   <li>RuntimeException (general errors → 500)</li>
 *   <li>All other exceptions (→ 500)</li>
 * </ul>
 * 
 * <p><b>Exception Hierarchy:</b>
 * <pre>
 * Exception
 *   ├─ WebApplicationException (JAX-RS exceptions)
 *   │   ├─ NotFoundException (404)
 *   │   ├─ BadRequestException (400)
 *   │   └─ ... other JAX-RS exceptions
 *   ├─ IllegalArgumentException → 400 Bad Request
 *   ├─ RuntimeException → 500 Internal Server Error
 *   └─ ... all others → 500
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);
    
    @Override
    public Response toResponse(Exception exception) {
        // Log the exception
        logException(exception);
        
        // Handle WebApplicationException (JAX-RS exceptions with status codes)
        if (exception instanceof WebApplicationException) {
            return handleWebApplicationException((WebApplicationException) exception);
        }
        
        // Handle IllegalArgumentException (validation errors)
        if (exception instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) exception);
        }
        
        // Handle all other exceptions as internal server errors
        return handleGenericException(exception);
    }
    
    /**
     * Handles WebApplicationException (JAX-RS exceptions).
     * These already have HTTP status codes assigned.
     */
    private Response handleWebApplicationException(WebApplicationException exception) {
        Response originalResponse = exception.getResponse();
        int status = originalResponse.getStatus();
        
        // Determine error code based on status
        String errorCode = getErrorCodeForStatus(status);
        
        // Use exception message or default message
        String message = exception.getMessage();
        if (message == null || message.isEmpty()) {
            message = getDefaultMessageForStatus(status);
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(errorCode)
            .message(message)
            .requestId(CorrelationIdContext.get())
            .build();
        
        return Response
            .status(status)
            .entity(errorResponse)
            .build();
    }
    
    /**
     * Handles IllegalArgumentException as bad request (400).
     * These are typically validation errors.
     */
    private Response handleIllegalArgumentException(IllegalArgumentException exception) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ErrorResponse.ErrorCodes.VALIDATION_ERROR)
            .message(exception.getMessage())
            .requestId(CorrelationIdContext.get())
            .build();
        
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(errorResponse)
            .build();
    }
    
    /**
     * Handles generic exceptions as internal server error (500).
     */
    private Response handleGenericException(Exception exception) {
        // Don't expose internal error details to clients
        ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ErrorResponse.ErrorCodes.INTERNAL_ERROR)
            .message("An internal server error occurred. Please contact support if the problem persists.")
            .requestId(CorrelationIdContext.get())
            .build();
        
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(errorResponse)
            .build();
    }
    
    /**
     * Logs the exception with appropriate level.
     */
    private void logException(Exception exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            int status = webEx.getResponse().getStatus();
            
            // 4xx errors are client errors (log as info/debug)
            if (status >= 400 && status < 500) {
                LOG.info("Client error: " + exception.getMessage(), exception);
            }
            // 5xx errors are server errors (log as error)
            else if (status >= 500) {
                LOG.error("Server error: " + exception.getMessage(), exception);
            }
        } else if (exception instanceof IllegalArgumentException) {
            // Validation errors (log as info)
            LOG.info("Validation error: " + exception.getMessage());
        } else {
            // All other exceptions (log as error)
            LOG.error("Unhandled exception: " + exception.getMessage(), exception);
        }
    }
    
    /**
     * Gets appropriate error code for HTTP status.
     */
    private String getErrorCodeForStatus(int status) {
        switch (status) {
            case 400:
                return ErrorResponse.ErrorCodes.BAD_REQUEST;
            case 401:
                return ErrorResponse.ErrorCodes.UNAUTHORIZED;
            case 403:
                return ErrorResponse.ErrorCodes.FORBIDDEN;
            case 404:
                return ErrorResponse.ErrorCodes.RESOURCE_NOT_FOUND;
            case 405:
                return ErrorResponse.ErrorCodes.METHOD_NOT_ALLOWED;
            case 409:
                return ErrorResponse.ErrorCodes.RESOURCE_CONFLICT;
            case 415:
                return ErrorResponse.ErrorCodes.UNSUPPORTED_MEDIA_TYPE;
            case 500:
                return ErrorResponse.ErrorCodes.INTERNAL_ERROR;
            case 503:
                return ErrorResponse.ErrorCodes.SERVICE_UNAVAILABLE;
            default:
                return "HTTP_" + status;
        }
    }
    
    /**
     * Gets default message for HTTP status.
     */
    private String getDefaultMessageForStatus(int status) {
        switch (status) {
            case 400:
                return "Bad request";
            case 401:
                return "Authentication required";
            case 403:
                return "Access forbidden";
            case 404:
                return "Resource not found";
            case 405:
                return "HTTP method not allowed";
            case 409:
                return "Resource conflict";
            case 415:
                return "Unsupported media type";
            case 500:
                return "Internal server error";
            case 503:
                return "Service temporarily unavailable";
            default:
                return "Request failed with status " + status;
        }
    }
}
