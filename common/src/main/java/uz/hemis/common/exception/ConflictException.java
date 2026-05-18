package uz.hemis.common.exception;

/**
 * Conflict Exception — operatsiya mavjud holatga zid (HTTP 409 CONFLICT).
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Duplicate PINFL (talaba qo'shishda)</li>
 *   <li>Duplicate university_code (OTM qo'shishda)</li>
 *   <li>WebhookTarget university uchun allaqachon mavjud</li>
 *   <li>Email allaqachon ro'yxatdan o'tgan</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
