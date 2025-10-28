package uz.hemis.admin.exception;

/**
 * Authentication exception
 *
 * Thrown when authentication fails (invalid credentials, disabled account, etc.)
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
