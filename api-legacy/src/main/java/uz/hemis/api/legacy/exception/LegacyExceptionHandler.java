package uz.hemis.api.legacy.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Legacy API Exception Handler
 *
 * <p>Old-hemis error format bilan mos javoblar qaytaradi</p>
 * <p>Faqat /app/rest/v2/entities/* endpointlar uchun</p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice(basePackages = "uz.hemis.api.legacy.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LegacyExceptionHandler {

    /**
     * Handle UUID conversion errors for entity endpoints
     *
     * <p>Old-hemis format:</p>
     * <pre>
     * {
     *   "error": "Invalid entity ID",
     *   "details": "Cannot convert xxx into valid entity ID"
     * }
     * </pre>
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();
        log.error("Legacy type mismatch at {}: {}", path, ex.getMessage());

        // Entity ID uchun old-hemis formati
        if ("entityId".equals(ex.getName()) || "id".equals(ex.getName())) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Invalid entity ID");
            error.put("details", "Cannot convert " + ex.getValue() + " into valid entity ID");
            return ResponseEntity.badRequest().body(error);
        }

        // Boshqa parametrlar uchun
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Invalid parameter");
        error.put("details", String.format("Parameter '%s' should be of type '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"));
        return ResponseEntity.badRequest().body(error);
    }
}
