package uz.hemis.common.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Validation Exception
 *
 * <p>Thrown when business validation fails.</p>
 *
 * <p><strong>HTTP Status:</strong> 400 BAD REQUEST</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Duplicate PINFL</li>
 *   <li>Invalid student status transition</li>
 *   <li>Business rule violations</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;

    /**
     * Constructor with message
     *
     * @param message error message
     */
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }

    /**
     * Constructor with message and errors
     *
     * @param message error message
     * @param errors field-level errors
     */
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors != null ? errors : new HashMap<>();
    }

    /**
     * Constructor with single field error
     *
     * @param message error message
     * @param field field name
     * @param error error message for field
     */
    public ValidationException(String message, String field, String error) {
        super(message);
        this.errors = new HashMap<>();
        this.errors.put(field, error);
    }

    /**
     * Get validation errors
     *
     * @return field-level errors map
     */
    public Map<String, String> getErrors() {
        return errors;
    }

    /**
     * Check if has field-level errors
     *
     * @return true if has errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
