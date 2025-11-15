package uz.hemis.web.controller.exception;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.hemis.common.dto.ErrorResponse;

/**
 * Web API Exception Handler
 *
 * <p><strong>Purpose:</strong> Handle exceptions for api-web module controllers</p>
 *
 * <p><strong>Scope:</strong> Only handles exceptions from uz.hemis.web.controller package</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Map authentication exceptions to HTTP 401 Unauthorized</li>
 *   <li>Map other exceptions to appropriate HTTP status codes</li>
 *   <li>Return standardized ErrorResponse format</li>
 * </ul>
 *
 * <p><strong>CRITICAL - Frontend Compatibility:</strong></p>
 * <ul>
 *   <li>401 for authentication failures (wrong username/password)</li>
 *   <li>Frontend expects JSON error response, not 500</li>
 *   <li>Error messages should be user-friendly</li>
 * </ul>
 *
 * @since 1.0.0
 */
@RestControllerAdvice(basePackages = "uz.hemis.web.controller")
@Slf4j
public class WebExceptionHandler {

    /**
     * Handle authentication failures (wrong username or password)
     *
     * <p><strong>Exceptions Handled:</strong></p>
     * <ul>
     *   <li>{@link UsernameNotFoundException} - User not found</li>
     *   <li>{@link BadCredentialsException} - Wrong password</li>
     * </ul>
     *
     * <p><strong>Response:</strong> HTTP 401 Unauthorized with JSON error</p>
     *
     * @param ex authentication exception
     * @param request HTTP request
     * @return 401 error response
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationErrors(
            Exception ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        // ⭐ Capture to Sentry with WARNING level (401 is not a critical error)
        String eventId = Sentry.captureException(ex, scope -> {
            scope.setLevel(SentryLevel.WARNING);
            scope.setTag("error_type", "authentication_failed");
            scope.setTag("error_code", "AUTH_FAILED");
            scope.setExtra("url", request.getRequestURI());
            scope.setExtra("method", request.getMethod());
        }).toString();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Login yoki parol noto'g'ri",
                request.getRequestURI(),
                eventId,  // ⭐ Sentry Event ID
                "AUTH_FAILED"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    /**
     * Handle generic runtime exceptions
     *
     * <p><strong>Response:</strong> HTTP 500 Internal Server Error</p>
     *
     * @param ex runtime exception
     * @param request HTTP request
     * @return 500 error response
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.error("Runtime exception in web controller: {}", ex.getMessage(), ex);

        // ⭐ Capture to Sentry with ERROR level (500 is a critical error)
        String eventId = Sentry.captureException(ex, scope -> {
            scope.setLevel(SentryLevel.ERROR);
            scope.setTag("error_type", "runtime_exception");
            scope.setTag("error_code", "INTERNAL_ERROR");
            scope.setExtra("url", request.getRequestURI());
            scope.setExtra("method", request.getMethod());
            scope.setExtra("user_agent", request.getHeader("User-Agent"));
        }).toString();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Serverda xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring.",
                request.getRequestURI(),
                eventId,  // ⭐ Sentry Event ID
                "INTERNAL_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
