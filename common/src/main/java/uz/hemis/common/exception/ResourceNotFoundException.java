package uz.hemis.common.exception;

/**
 * Resource Not Found Exception
 *
 * <p>Thrown when a requested resource does not exist in the database.</p>
 *
 * <p><strong>HTTP Status:</strong> 404 NOT FOUND</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Student not found by ID/PINFL</li>
 *   <li>University not found by code</li>
 *   <li>Teacher not found by ID</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructor with message
     *
     * @param message error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Constructor with resource details
     *
     * @param resourceName resource name (e.g., "Student", "University")
     * @param fieldName field name (e.g., "id", "code", "pinfl")
     * @param fieldValue field value
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Get resource name
     *
     * @return resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Get field name
     *
     * @return field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Get field value
     *
     * @return field value
     */
    public Object getFieldValue() {
        return fieldValue;
    }
}
