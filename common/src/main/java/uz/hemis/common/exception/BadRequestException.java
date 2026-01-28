package uz.hemis.common.exception;

/**
 * Bad Request Exception
 *
 * <p>Thrown when client sends invalid request.</p>
 *
 * <p><strong>HTTP Status:</strong> 400 BAD REQUEST</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Invalid request parameters</li>
 *   <li>Malformed JSON</li>
 *   <li>Invalid query parameters</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with message
     *
     * @param message error message
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message error message
     * @param cause root cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
