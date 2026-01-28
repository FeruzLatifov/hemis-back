package uz.hemis.web.controller.exception;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.service.I18nService;

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
 *   <li>Return localized error messages based on Accept-Language header</li>
 * </ul>
 *
 * <p><strong>CRITICAL - Backend-driven i18n (Industrial Best Practice):</strong></p>
 * <ul>
 *   <li>Error messages are translated by backend based on Accept-Language header</li>
 *   <li>Frontend receives ready-to-display localized messages</li>
 *   <li>Single Source of Truth - all translations in database</li>
 *   <li>Works for Mobile + Web - same API, same translations</li>
 * </ul>
 *
 * @since 1.0.0
 */
@RestControllerAdvice(basePackages = "uz.hemis.web.controller")
@RequiredArgsConstructor
@Slf4j
public class WebExceptionHandler {

    private final I18nService i18nService;

    /**
     * Handle authentication failures (wrong username or password)
     *
     * <p><strong>Exceptions Handled:</strong></p>
     * <ul>
     *   <li>{@link UsernameNotFoundException} - User not found</li>
     *   <li>{@link BadCredentialsException} - Wrong password</li>
     * </ul>
     *
     * <p><strong>Response:</strong> HTTP 401 Unauthorized with localized JSON error</p>
     *
     * <p><strong>Backend-driven i18n:</strong></p>
     * <ul>
     *   <li>Accept-Language: uz-UZ → "Login yoki parol noto'g'ri"</li>
     *   <li>Accept-Language: ru-RU → "Неверный логин или пароль"</li>
     *   <li>Accept-Language: en-US → "Invalid username or password"</li>
     * </ul>
     *
     * @param ex authentication exception
     * @param request HTTP request
     * @param language Accept-Language header (default: uz-UZ)
     * @return 401 error response with localized message
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationErrors(
            Exception ex,
            HttpServletRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "uz-UZ") String language
    ) {
        log.warn("Authentication failed: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        // ⭐ Get localized error message from database
        String localizedMessage = i18nService.getMessage("error.auth.failed", language);

        // ⭐ Capture to Sentry with WARNING level (401 is not a critical error)
        String eventId = Sentry.captureException(ex, scope -> {
            scope.setLevel(SentryLevel.WARNING);
            scope.setTag("error_type", "authentication_failed");
            scope.setTag("error_code", "AUTH_FAILED");
            scope.setTag("language", language);
            scope.setExtra("url", request.getRequestURI());
            scope.setExtra("method", request.getMethod());
        }).toString();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                localizedMessage,  // ⭐ Localized message from database
                request.getRequestURI(),
                eventId,
                "AUTH_FAILED"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    /**
     * Handle generic runtime exceptions
     *
     * <p><strong>Response:</strong> HTTP 500 Internal Server Error with localized message</p>
     *
     * <p><strong>Backend-driven i18n:</strong></p>
     * <ul>
     *   <li>Accept-Language: uz-UZ → "Serverda xatolik yuz berdi..."</li>
     *   <li>Accept-Language: ru-RU → "Ошибка сервера..."</li>
     *   <li>Accept-Language: en-US → "Server error..."</li>
     * </ul>
     *
     * @param ex runtime exception
     * @param request HTTP request
     * @param language Accept-Language header (default: uz-UZ)
     * @return 500 error response with localized message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "uz-UZ") String language
    ) {
        log.error("Runtime exception in web controller: {}", ex.getMessage(), ex);

        // ⭐ Get localized error message from database
        String localizedMessage = i18nService.getMessage("error.internal", language);

        // ⭐ Capture to Sentry with ERROR level (500 is a critical error)
        String eventId = Sentry.captureException(ex, scope -> {
            scope.setLevel(SentryLevel.ERROR);
            scope.setTag("error_type", "runtime_exception");
            scope.setTag("error_code", "INTERNAL_ERROR");
            scope.setTag("language", language);
            scope.setExtra("url", request.getRequestURI());
            scope.setExtra("method", request.getMethod());
            scope.setExtra("user_agent", request.getHeader("User-Agent"));
        }).toString();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                localizedMessage,  // ⭐ Localized message from database
                request.getRequestURI(),
                eventId,
                "INTERNAL_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    /**
     * Handle access denied exceptions
     *
     * <p><strong>Response:</strong> HTTP 403 Forbidden with localized message</p>
     *
     * @param ex access denied exception
     * @param request HTTP request
     * @param language Accept-Language header (default: uz-UZ)
     * @return 403 error response with localized message
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "uz-UZ") String language
    ) {
        log.warn("Access denied: {} - {}", request.getRequestURI(), ex.getMessage());

        // ⭐ Get localized error message from database
        String localizedMessage = i18nService.getMessage("error.auth.access_denied", language);

        // ⭐ Capture to Sentry with WARNING level
        String eventId = Sentry.captureException(ex, scope -> {
            scope.setLevel(SentryLevel.WARNING);
            scope.setTag("error_type", "access_denied");
            scope.setTag("error_code", "ACCESS_DENIED");
            scope.setTag("language", language);
            scope.setExtra("url", request.getRequestURI());
            scope.setExtra("method", request.getMethod());
        }).toString();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                localizedMessage,
                request.getRequestURI(),
                eventId,
                "ACCESS_DENIED"
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }
}
