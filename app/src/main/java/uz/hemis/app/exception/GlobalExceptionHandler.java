package uz.hemis.app.exception;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 *
 * <p><strong>@RestControllerAdvice:</strong> Handles exceptions across all @RestController</p>
 *
 * <p><strong>CRITICAL - Legacy Error Format:</strong></p>
 * <ul>
 *   <li>Error responses must match legacy format (if exists)</li>
 *   <li>HTTP status codes preserved</li>
 *   <li>Error messages in expected format</li>
 * </ul>
 *
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =====================================================
    // Custom Business Exceptions
    // =====================================================

    /**
     * Handle ResourceNotFoundException
     *
     * <p>HTTP Status: 404 NOT FOUND</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle ValidationException
     *
     * <p>HTTP Status: 400 BAD REQUEST</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex,
            HttpServletRequest request
    ) {
        log.error("Validation failed: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();

        if (ex.hasErrors()) {
            fieldErrors = ex.getErrors().entrySet().stream()
                    .map(entry -> ErrorResponse.FieldError.builder()
                            .field(entry.getKey())
                            .message(entry.getValue())
                            .build())
                    .collect(Collectors.toList());
        }

        ErrorResponse error = ErrorResponse.validationError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle BadRequestException
     *
     * <p>HTTP Status: 400 BAD REQUEST</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        log.error("Bad request: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // =====================================================
    // Spring Security Exceptions
    // =====================================================

    /**
     * Handle AccessDeniedException (Spring Security 5.x)
     *
     * <p>HTTP Status: 403 FORBIDDEN</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied: {} - User attempted to access: {}", 
                ex.getMessage(), request.getRequestURI());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You don't have permission to access this resource",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle AuthorizationDeniedException (Spring Security 6.x)
     *
     * <p>HTTP Status: 403 FORBIDDEN</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Authorization denied: {} - User attempted to access: {}", 
                ex.getMessage(), request.getRequestURI());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You don't have permission to access this resource",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // =====================================================
    // Spring Validation Exceptions
    // =====================================================

    /**
     * Handle MethodArgumentNotValidException
     *
     * <p>Thrown when @Valid fails on @RequestBody</p>
     * <p>HTTP Status: 400 BAD REQUEST</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.error("Method argument validation failed");

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError
                            ? ((FieldError) error).getField()
                            : error.getObjectName();

                    Object rejectedValue = error instanceof FieldError
                            ? ((FieldError) error).getRejectedValue()
                            : null;

                    return ErrorResponse.FieldError.builder()
                            .field(fieldName)
                            .rejectedValue(rejectedValue)
                            .message(error.getDefaultMessage())
                            .code(error.getCode())
                            .build();
                })
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.validationError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for request body",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle ConstraintViolationException
     *
     * <p>Thrown when @Validated fails on method parameters</p>
     * <p>HTTP Status: 400 BAD REQUEST</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.error("Constraint violation: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> ErrorResponse.FieldError.builder()
                        .field(getFieldName(violation))
                        .rejectedValue(violation.getInvalidValue())
                        .message(violation.getMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.validationError(
                HttpStatus.BAD_REQUEST.value(),
                "Constraint violation",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // =====================================================
    // HTTP Message Conversion Exceptions
    // =====================================================

    /**
     * Handle HttpMessageNotReadableException
     *
     * <p>Thrown when request body is malformed JSON</p>
     * <p>HTTP Status: 400 BAD REQUEST</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.error("Malformed JSON request: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON",
                "Request body contains invalid JSON",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle MethodArgumentTypeMismatchException
     *
     * <p>Thrown when path variable or request param has wrong type</p>
     * <p>HTTP Status: 400 BAD REQUEST</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        log.error("Method argument type mismatch: {}", ex.getMessage());

        String message = String.format(
                "Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Type Mismatch",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // =====================================================
    // Generic Exception Handler
    // =====================================================

    /**
     * Handle all other exceptions
     *
     * <p>HTTP Status: 500 INTERNAL SERVER ERROR</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);

        // Capture to Sentry (auto-captures if enabled)
        String eventId = Sentry.captureException(ex).toString();

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                eventId,
                "INTERNAL_ERROR"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Map missing static resources (e.g., /swagger-ui) to 404 instead of 500
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    /**
     * Extract field name from constraint violation
     *
     * @param violation constraint violation
     * @return field name
     */
    private String getFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
}
