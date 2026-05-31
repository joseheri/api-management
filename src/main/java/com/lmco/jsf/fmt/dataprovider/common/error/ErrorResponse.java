package com.lmco.jsf.fmt.dataprovider.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response format for API endpoints.
 * 
 * <p>Provides consistent error structure across all API responses, matching
 * the OpenAPI specification error format.
 * 
 * <p><b>Response Structure:</b>
 * <pre>
 * {
 *   "error": {
 *     "code": "INVALID_TIME_PARAMS",
 *     "message": "Cannot use 'since' with 'from' or 'to'",
 *     "details": {
 *       "received": ["since", "from"],
 *       "validCombinations": ["since (alone)", "from and/or to"]
 *     },
 *     "timestamp": "2026-05-20T22:30:00Z",
 *     "request_id": "req-12345"
 *   }
 * }
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final ErrorDetail error;
    
    /**
     * Creates an error response.
     * 
     * @param error the error detail
     */
    public ErrorResponse(ErrorDetail error) {
        this.error = error;
    }
    
    /**
     * Creates an error response with code and message.
     * 
     * @param code machine-readable error code
     * @param message human-readable error message
     */
    public ErrorResponse(String code, String message) {
        this(new ErrorDetail(code, message));
    }
    
    /**
     * Gets the error detail.
     * 
     * @return the error detail
     */
    public ErrorDetail getError() {
        return error;
    }
    
    /**
     * Detailed error information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final Map<String, Object> details;
        private final Instant timestamp;
        private final String requestId;
        
        /**
         * Creates error detail with all fields.
         */
        public ErrorDetail(String code, String message, Map<String, Object> details, 
                          Instant timestamp, String requestId) {
            this.code = code;
            this.message = message;
            this.details = details;
            this.timestamp = timestamp != null ? timestamp : Instant.now();
            this.requestId = requestId;
        }
        
        /**
         * Creates error detail with code and message only.
         */
        public ErrorDetail(String code, String message) {
            this(code, message, null, Instant.now(), null);
        }
        
        /**
         * Gets the machine-readable error code.
         * 
         * @return error code (e.g., "INVALID_TIME_PARAMS")
         */
        public String getCode() {
            return code;
        }
        
        /**
         * Gets the human-readable error message.
         * 
         * @return error message
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Gets additional error context/details.
         * 
         * @return map of detail fields, or null if none
         */
        public Map<String, Object> getDetails() {
            return details;
        }
        
        /**
         * Gets the timestamp when error occurred.
         * 
         * @return ISO-8601 timestamp
         */
        public Instant getTimestamp() {
            return timestamp;
        }
        
        /**
         * Gets the unique request identifier for troubleshooting.
         * 
         * @return request ID, or null if not set
         */
        public String getRequestId() {
            return requestId;
        }
    }
    
    /**
     * Builder for creating ErrorResponse instances.
     */
    public static class Builder {
        private String code;
        private String message;
        private Map<String, Object> details;
        private Instant timestamp;
        private String requestId;
        
        /**
         * Sets the error code.
         * 
         * @param code machine-readable error code
         * @return this builder
         */
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        /**
         * Sets the error message.
         * 
         * @param message human-readable error message
         * @return this builder
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        /**
         * Sets error details.
         * 
         * @param details map of additional context
         * @return this builder
         */
        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }
        
        /**
         * Adds a single detail field.
         * 
         * @param key the detail field name
         * @param value the detail value
         * @return this builder
         */
        public Builder detail(String key, Object value) {
            if (this.details == null) {
                this.details = new HashMap<>();
            }
            this.details.put(key, value);
            return this;
        }
        
        /**
         * Sets the error timestamp.
         * 
         * @param timestamp when the error occurred
         * @return this builder
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        /**
         * Sets the request ID.
         * 
         * @param requestId unique request identifier
         * @return this builder
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        /**
         * Builds the ErrorResponse.
         * 
         * @return the error response
         * @throws IllegalArgumentException if code or message is missing
         */
        public ErrorResponse build() {
            if (code == null || code.isEmpty()) {
                throw new IllegalArgumentException("Error code is required");
            }
            if (message == null || message.isEmpty()) {
                throw new IllegalArgumentException("Error message is required");
            }
            
            ErrorDetail detail = new ErrorDetail(code, message, details, timestamp, requestId);
            return new ErrorResponse(detail);
        }
    }
    
    /**
     * Creates a new builder for constructing ErrorResponse.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Common error codes used across the API.
     */
    public static final class ErrorCodes {
        // Time parameter errors
        public static final String INVALID_TIME_PARAMS = "INVALID_TIME_PARAMS";
        public static final String INVALID_TIME_RANGE = "INVALID_TIME_RANGE";
        public static final String INVALID_TIMESTAMP_FORMAT = "INVALID_TIMESTAMP_FORMAT";
        
        // Pagination errors
        public static final String INVALID_PAGINATION = "INVALID_PAGINATION";
        public static final String OFFSET_OUT_OF_RANGE = "OFFSET_OUT_OF_RANGE";
        
        // Resource errors
        public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
        public static final String RESOURCE_CONFLICT = "RESOURCE_CONFLICT";
        
        // Authentication/Authorization errors
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String INVALID_TOKEN = "INVALID_TOKEN";
        public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
        
        // Validation errors
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
        public static final String MISSING_REQUIRED_FIELD = "MISSING_REQUIRED_FIELD";
        public static final String INVALID_FIELD_VALUE = "INVALID_FIELD_VALUE";
        
        // Server errors
        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
        public static final String DATABASE_ERROR = "DATABASE_ERROR";
        public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
        
        // Request errors
        public static final String BAD_REQUEST = "BAD_REQUEST";
        public static final String UNSUPPORTED_MEDIA_TYPE = "UNSUPPORTED_MEDIA_TYPE";
        public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
        
        // API Key Management errors
        public static final String API_KEY_INVALID = "API_KEY_INVALID";
        public static final String API_KEY_EXPIRED = "API_KEY_EXPIRED";
        public static final String API_KEY_REVOKED = "API_KEY_REVOKED";
        public static final String API_KEY_DISABLED = "API_KEY_DISABLED";
        public static final String API_KEY_MALFORMED = "API_KEY_MALFORMED";
        public static final String API_KEY_MISSING = "API_KEY_MISSING";
        public static final String API_KEY_CLIENT_DISABLED = "API_KEY_CLIENT_DISABLED";
        public static final String API_KEY_SCOPE_MISSING = "API_KEY_SCOPE_MISSING";
        
        // API Client errors
        public static final String API_CLIENT_NOT_FOUND = "API_CLIENT_NOT_FOUND";
        public static final String API_CLIENT_ALREADY_DISABLED = "API_CLIENT_ALREADY_DISABLED";
        public static final String API_CLIENT_INVALID_ENVIRONMENT = "API_CLIENT_INVALID_ENVIRONMENT";
        
        // Claim Invitation errors
        public static final String CLAIM_INVITATION_NOT_FOUND = "CLAIM_INVITATION_NOT_FOUND";
        public static final String CLAIM_INVITATION_EXPIRED = "CLAIM_INVITATION_EXPIRED";
        public static final String CLAIM_INVITATION_ALREADY_CLAIMED = "CLAIM_INVITATION_ALREADY_CLAIMED";
        public static final String CLAIM_INVITATION_REVOKED = "CLAIM_INVITATION_REVOKED";
        public static final String CLAIM_INVITATION_LOCKED = "CLAIM_INVITATION_LOCKED";
        public static final String CLAIM_CODE_INVALID = "CLAIM_CODE_INVALID";
        public static final String CLAIM_EMAIL_MISMATCH = "CLAIM_EMAIL_MISMATCH";
        public static final String CLAIM_APPROVAL_REQUIRED = "CLAIM_APPROVAL_REQUIRED";
        
        // Scope errors
        public static final String SCOPE_INVALID = "SCOPE_INVALID";
        public static final String SCOPE_NOT_ALLOWED = "SCOPE_NOT_ALLOWED";
        
        private ErrorCodes() {
            throw new UnsupportedOperationException("Constants class cannot be instantiated");
        }
    }
}
