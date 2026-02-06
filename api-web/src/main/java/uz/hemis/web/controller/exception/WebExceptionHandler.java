package uz.hemis.web.controller.exception;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.common.exception.ExceptionHandlerUtils;
import uz.hemis.service.shared.I18nService;

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
 *   <li>Return localized error messages based on Accept-Language header</li>
 * </ul>
 *
 * <p>All other exceptions (AccessDeniedException, RuntimeException, etc.)
 * are handled by GlobalExceptionHandler.</p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice(basePackages = "uz.hemis.web.controller")
@RequiredArgsConstructor
@Slf4j
public class WebExceptionHandler {

    private static final String DEFAULT_LANGUAGE = "uz-UZ";

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
     * @param ex authentication exception
     * @param request HTTP request
     * @return 401 error response with localized message
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationErrors(
            Exception ex,
            HttpServletRequest request
    ) {
        String language = ExceptionHandlerUtils.extractLanguage(request.getHeader("Accept-Language"), DEFAULT_LANGUAGE);
        log.warn("Authentication failed: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        String localizedMessage = i18nService.getMessage("Invalid username or password", language);

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
                localizedMessage,
                request.getRequestURI(),
                eventId,
                "AUTH_FAILED"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }
}
