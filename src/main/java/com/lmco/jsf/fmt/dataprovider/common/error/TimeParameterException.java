package com.lmco.jsf.fmt.dataprovider.common.error;

import java.util.List;
import java.util.Map;

/**
 * Exception for time parameter validation errors.
 * 
 * <p>Thrown when time filter parameters (since, from, to) are invalid or
 * have conflicting values. Provides detailed error information that can be
 * mapped to structured error responses.
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public class TimeParameterException extends IllegalArgumentException {
    
    private final String errorCode;
    private final Map<String, Object> details;
    
    /**
     * Creates a time parameter exception with code, message, and details.
     * 
     * @param errorCode the error code (e.g., "INVALID_TIME_PARAMS")
     * @param message the error message
     * @param details additional error context
     */
    public TimeParameterException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    /**
     * Creates a time parameter exception with code and message.
     * 
     * @param errorCode the error code
     * @param message the error message
     */
    public TimeParameterException(String errorCode, String message) {
        this(errorCode, message, null);
    }
    
    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets additional error details.
     * 
     * @return map of error details, or null if none
     */
    public Map<String, Object> getDetails() {
        return details;
    }
    
    /**
     * Creates an exception for conflicting time parameters.
     * Thrown when 'since' is used with 'from' or 'to'.
     * 
     * @param receivedParams the conflicting parameters that were received
     * @return the exception
     */
    public static TimeParameterException conflictingParameters(List<String> receivedParams) {
        String message = "Cannot use 'since' parameter with 'from' or 'to'. " +
                        "Use either delta sync (since) or time range (from/to).";
        
        Map<String, Object> details = Map.of(
            "received", receivedParams,
            "validCombinations", List.of("since (alone)", "from and/or to")
        );
        
        return new TimeParameterException(
            ErrorResponse.ErrorCodes.INVALID_TIME_PARAMS,
            message,
            details
        );
    }
    
    /**
     * Creates an exception for invalid time range.
     * Thrown when 'from' timestamp is after 'to' timestamp.
     * 
     * @param from the from timestamp
     * @param to the to timestamp
     * @return the exception
     */
    public static TimeParameterException invalidTimeRange(String from, String to) {
        String message = String.format(
            "'from' timestamp (%s) must be before or equal to 'to' timestamp (%s).",
            from, to
        );
        
        Map<String, Object> details = Map.of(
            "from", from,
            "to", to,
            "issue", "from > to"
        );
        
        return new TimeParameterException(
            ErrorResponse.ErrorCodes.INVALID_TIME_RANGE,
            message,
            details
        );
    }
    
    /**
     * Creates an exception for invalid timestamp format.
     * Thrown when timestamp string cannot be parsed.
     * 
     * @param paramName the parameter name (since, from, or to)
     * @param value the invalid timestamp value
     * @return the exception
     */
    public static TimeParameterException invalidTimestampFormat(String paramName, String value) {
        String message = String.format(
            "Invalid '%s' timestamp format: '%s'. Expected ISO-8601 format (e.g., 2026-05-01T00:00:00Z).",
            paramName, value
        );
        
        Map<String, Object> details = Map.of(
            "parameter", paramName,
            "value", value,
            "expectedFormat", "ISO-8601 (yyyy-MM-dd'T'HH:mm:ss'Z')"
        );
        
        return new TimeParameterException(
            ErrorResponse.ErrorCodes.INVALID_TIMESTAMP_FORMAT,
            message,
            details
        );
    }
    
    /**
     * Creates an exception for time range that's too large.
     * Thrown when the range between from and to exceeds maximum allowed days.
     * 
     * @param actualDays the actual range in days
     * @param maxDays the maximum allowed days
     * @return the exception
     */
    public static TimeParameterException rangeTooLarge(long actualDays, int maxDays) {
        String message = String.format(
            "Time range is too large (%d days). Maximum allowed range is %d days.",
            actualDays, maxDays
        );
        
        Map<String, Object> details = Map.of(
            "actualDays", actualDays,
            "maxDays", maxDays
        );
        
        return new TimeParameterException(
            ErrorResponse.ErrorCodes.INVALID_TIME_RANGE,
            message,
            details
        );
    }
}
