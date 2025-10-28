package uz.hemis.admin.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler
 *
 * Handles all exceptions and returns standardized error responses
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("AUTHENTICATION_ERROR")
                        .message(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("RESOURCE_NOT_FOUND")
                        .message(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("VALIDATION_ERROR")
                        .message(ex.getMessage())
                        .details(ex.getErrors())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle Spring validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("VALIDATION_ERROR")
                        .message("Validation failed")
                        .details(errors)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error: ", ex);

        ErrorResponse error = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("An unexpected error occurred")
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response structure
     */
    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {
        private boolean success;
        private ErrorDetail error;
        private LocalDateTime timestamp;
    }

    /**
     * Error detail structure
     */
    @lombok.Data
    @lombok.Builder
    public static class ErrorDetail {
        private String code;
        private String message;
        private Map<String, String> details;
    }
}
