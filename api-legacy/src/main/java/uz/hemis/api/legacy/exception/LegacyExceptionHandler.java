package uz.hemis.api.legacy.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uz.hemis.common.exception.ResourceNotFoundException;

import java.util.*;

/**
 * Legacy API Exception Handler
 *
 * <p>Old-hemis error format bilan mos javoblar qaytaradi</p>
 * <p>Faqat /app/rest/v2/entities/* endpointlar uchun</p>
 *
 * <p>OLD-HEMIS validation error format (CUBA Platform):</p>
 * <pre>
 * [
 *   {"id": "fieldName", "message": "may not be null"},
 *   {"id": "fieldName2", "message": "may not be empty"}
 * ]
 * </pre>
 *
 * @since 1.0.0
 */
@RestControllerAdvice(basePackages = "uz.hemis.api.legacy.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LegacyExceptionHandler {

    /**
     * Handle UUID conversion errors for entity endpoints
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        log.error("Legacy type mismatch at {}: {}", path, ex.getMessage());

        if ("entityId".equals(ex.getName()) || "id".equals(ex.getName())) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Invalid entity ID");
            error.put("details", "Cannot convert " + ex.getValue() + " into valid entity ID");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Invalid parameter");
        error.put("details", String.format("Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"));
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle Bean Validation errors (@Valid)
     * OLD-HEMIS format: array of {id, message}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.warn("Validation error at {}: {}", request.getRequestURI(), ex.getMessage());

        List<Map<String, String>> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("id", fe.getField());
            error.put("message", fe.getDefaultMessage());
            errors.add(error);
        });

        if (errors.isEmpty()) {
            ex.getBindingResult().getGlobalErrors().forEach(ge -> {
                Map<String, String> error = new LinkedHashMap<>();
                error.put("id", ge.getObjectName());
                error.put("message", ge.getDefaultMessage());
                errors.add(error);
            });
        }

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handle Jakarta Constraint Violations
     * OLD-HEMIS format: array of {id, message}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Constraint violation at {}: {}", request.getRequestURI(), ex.getMessage());

        List<Map<String, String>> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            Map<String, String> error = new LinkedHashMap<>();
            String path = violation.getPropertyPath().toString();
            error.put("id", path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path);
            error.put("message", violation.getMessage());
            errors.add(error);
        }

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handle malformed JSON requests
     * OLD-HEMIS format: array of {id, message}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<Map<String, String>>> handleBadJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Bad JSON at {}: {}", request.getRequestURI(), ex.getMessage());

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new LinkedHashMap<>();
        error.put("id", "request");
        error.put("message", "Malformed JSON request");
        errors.add(error);

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handle DB constraint violations (duplicate key, FK violations, etc.)
     * OLD-HEMIS format: array of {id, message}
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<List<Map<String, String>>> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.error("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMessage());

        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> error = new LinkedHashMap<>();
        error.put("id", "database");

        String msg = ex.getMostSpecificCause().getMessage();
        if (msg != null && msg.contains("duplicate key")) {
            error.put("message", "Duplicate entry: record already exists");
        } else if (msg != null && msg.contains("not-null")) {
            error.put("message", "Required field is missing");
        } else if (msg != null && msg.contains("foreign key")) {
            error.put("message", "Referenced record not found");
        } else {
            error.put("message", "Data integrity violation");
        }
        errors.add(error);

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handle resource not found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Not found");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle generic runtime exceptions — return 400 instead of 500
     * OLD-HEMIS format varies but generally returns error details
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        log.error("Runtime exception at {}: {}", path, ex.getMessage(), ex);

        // POST/PUT da runtime exception -> 400 (validation-like error)
        String method = request.getMethod();
        if ("POST".equals(method) || "PUT".equals(method)) {
            List<Map<String, String>> errors = new ArrayList<>();
            Map<String, String> error = new LinkedHashMap<>();
            error.put("id", "server");
            error.put("message", ex.getMessage() != null ? ex.getMessage() : "Internal error");
            errors.add(error);
            return ResponseEntity.badRequest().body(errors);
        }

        // GET/DELETE uchun object formatida
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Server error");
        error.put("details", ex.getMessage() != null ? ex.getMessage() : "");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
