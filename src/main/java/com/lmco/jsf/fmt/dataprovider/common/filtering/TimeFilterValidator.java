package com.lmco.jsf.fmt.dataprovider.common.filtering;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates time filter parameters for API requests.
 * 
 * <p>Performs validation checks including:
 * <ul>
 *   <li>Parameter combination validity (since vs from/to conflict)</li>
 *   <li>Timestamp format validation (ISO-8601)</li>
 *   <li>Logical consistency (from before to)</li>
 *   <li>Reasonable time ranges (not too far in past/future)</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>
 * TimeFilterValidator validator = new TimeFilterValidator();
 * ValidationResult result = validator.validate(since, from, to);
 * 
 * if (!result.isValid()) {
 *     // Handle validation errors
 *     throw new BadRequestException(result.getErrorMessage());
 * }
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-20
 */
public class TimeFilterValidator {
    
    /**
     * Maximum allowed time range in days (default: 365 days / 1 year).
     * Prevents excessive queries spanning too long a period.
     */
    private final int maxRangeDays;
    
    /**
     * Whether to enforce maximum range limit.
     */
    private final boolean enforceMaxRange;
    
    /**
     * Creates a validator with default settings.
     * Max range: 365 days, enforced.
     */
    public TimeFilterValidator() {
        this(365, true);
    }
    
    /**
     * Creates a validator with custom settings.
     * 
     * @param maxRangeDays maximum allowed time range in days
     * @param enforceMaxRange whether to enforce the max range limit
     */
    public TimeFilterValidator(int maxRangeDays, boolean enforceMaxRange) {
        this.maxRangeDays = maxRangeDays;
        this.enforceMaxRange = enforceMaxRange;
    }
    
    /**
     * Validates time filter parameters.
     * 
     * @param since delta sync timestamp string (can be null)
     * @param from range start timestamp string (can be null)
     * @param to range end timestamp string (can be null)
     * @return validation result with success status and any error messages
     */
    public ValidationResult validate(String since, String from, String to) {
        List<String> errors = new ArrayList<>();
        
        // Check for conflicting parameters
        if (since != null && !since.isEmpty()) {
            if ((from != null && !from.isEmpty()) || (to != null && !to.isEmpty())) {
                errors.add("Cannot use 'since' parameter with 'from' or 'to'. " +
                          "Use either delta sync (since) or time range (from/to).");
                return ValidationResult.invalid(errors);
            }
        }
        
        // Parse and validate timestamps
        Instant sinceInstant = null;
        Instant fromInstant = null;
        Instant toInstant = null;
        
        if (since != null && !since.isEmpty()) {
            try {
                sinceInstant = Instant.parse(since);
            } catch (DateTimeParseException e) {
                errors.add(String.format(
                    "Invalid 'since' timestamp format: '%s'. Expected ISO-8601 format (e.g., 2026-05-01T00:00:00Z).",
                    since
                ));
            }
        }
        
        if (from != null && !from.isEmpty()) {
            try {
                fromInstant = Instant.parse(from);
            } catch (DateTimeParseException e) {
                errors.add(String.format(
                    "Invalid 'from' timestamp format: '%s'. Expected ISO-8601 format (e.g., 2026-05-01T00:00:00Z).",
                    from
                ));
            }
        }
        
        if (to != null && !to.isEmpty()) {
            try {
                toInstant = Instant.parse(to);
            } catch (DateTimeParseException e) {
                errors.add(String.format(
                    "Invalid 'to' timestamp format: '%s'. Expected ISO-8601 format (e.g., 2026-05-01T00:00:00Z).",
                    to
                ));
            }
        }
        
        // If there were parse errors, return early
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        // Validate logical consistency
        if (fromInstant != null && toInstant != null) {
            if (fromInstant.isAfter(toInstant)) {
                errors.add(String.format(
                    "'from' timestamp (%s) must be before or equal to 'to' timestamp (%s).",
                    from, to
                ));
            }
            
            // Check max range if enforced
            if (enforceMaxRange) {
                long daysBetween = java.time.Duration.between(fromInstant, toInstant).toDays();
                if (daysBetween > maxRangeDays) {
                    errors.add(String.format(
                        "Time range is too large (%d days). Maximum allowed range is %d days.",
                        daysBetween, maxRangeDays
                    ));
                }
            }
        }
        
        // Validate timestamps are not too far in the future
        Instant now = Instant.now();
        Instant maxFuture = now.plusSeconds(86400 * 30); // 30 days in future
        
        if (sinceInstant != null && sinceInstant.isAfter(maxFuture)) {
            errors.add(String.format(
                "'since' timestamp (%s) is too far in the future.",
                since
            ));
        }
        
        if (fromInstant != null && fromInstant.isAfter(maxFuture)) {
            errors.add(String.format(
                "'from' timestamp (%s) is too far in the future.",
                from
            ));
        }
        
        if (toInstant != null && toInstant.isAfter(maxFuture)) {
            errors.add(String.format(
                "'to' timestamp (%s) is too far in the future.",
                to
            ));
        }
        
        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }
    
    /**
     * Validates a TimeFilter object.
     * 
     * @param filter the time filter to validate
     * @return validation result
     */
    public ValidationResult validate(TimeFilter filter) {
        if (filter == null) {
            return ValidationResult.invalid(List.of("TimeFilter cannot be null"));
        }
        
        if (filter.isEmpty()) {
            return ValidationResult.valid(); // Empty filter is always valid
        }
        
        List<String> errors = new ArrayList<>();
        
        if (filter.isDelta() && filter.getSince() == null) {
            errors.add("Delta filter must have 'since' timestamp");
        }
        
        if (filter.isRange() && filter.getFrom() == null && filter.getTo() == null) {
            errors.add("Range filter must have at least 'from' or 'to' timestamp");
        }
        
        // Validate range consistency
        if (filter.isClosedRange()) {
            Instant from = filter.getFrom();
            Instant to = filter.getTo();
            
            if (from.isAfter(to)) {
                errors.add(String.format(
                    "'from' timestamp (%s) must be before or equal to 'to' timestamp (%s).",
                    from, to
                ));
            }
            
            if (enforceMaxRange) {
                long daysBetween = java.time.Duration.between(from, to).toDays();
                if (daysBetween > maxRangeDays) {
                    errors.add(String.format(
                        "Time range is too large (%d days). Maximum allowed range is %d days.",
                        daysBetween, maxRangeDays
                    ));
                }
            }
        }
        
        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }
    
    /**
     * Validates and parses timestamp string.
     * 
     * @param timestamp the timestamp string to parse
     * @param paramName the parameter name (for error messages)
     * @return parsed Instant, or null if timestamp is null/empty
     * @throws IllegalArgumentException if timestamp format is invalid
     */
    public static Instant parseAndValidate(String timestamp, String paramName) {
        if (timestamp == null || timestamp.isEmpty()) {
            return null;
        }
        
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid '%s' timestamp format: '%s'. Expected ISO-8601 format (e.g., 2026-05-01T00:00:00Z).",
                    paramName, timestamp
                ),
                e
            );
        }
    }
    
    /**
     * Represents the result of a validation operation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? List.copyOf(errors) : List.of();
        }
        
        /**
         * Creates a valid result.
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        /**
         * Creates an invalid result with error messages.
         */
        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }
        
        /**
         * Creates an invalid result with a single error message.
         */
        public static ValidationResult invalid(String error) {
            return new ValidationResult(false, List.of(error));
        }
        
        /**
         * Checks if validation passed.
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Gets the list of validation error messages.
         */
        public List<String> getErrors() {
            return errors;
        }
        
        /**
         * Gets a single combined error message.
         */
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
        
        /**
         * Throws an exception if validation failed.
         * 
         * @throws IllegalArgumentException if validation failed
         */
        public void throwIfInvalid() {
            if (!valid) {
                throw new IllegalArgumentException(getErrorMessage());
            }
        }
        
        @Override
        public String toString() {
            return valid ? "Valid" : "Invalid: " + getErrorMessage();
        }
    }
}
