package uz.hemis.common.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import uz.hemis.common.dto.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring-free utility methods for exception handlers.
 *
 * <p><strong>Module boundary (per common/CLAUDE.md):</strong> {@code common} must NOT depend
 * on Spring. Only Jakarta Validation API and pure Java are allowed here.</p>
 *
 * <p>Spring-coupled helpers (e.g. {@code MethodArgumentNotValidException} translation)
 * live in {@code uz.hemis.app.exception.SpringValidationUtils}.</p>
 *
 * @since 1.0.0
 */
public final class ExceptionHandlerUtils {

    private ExceptionHandlerUtils() {
        // utility class
    }

    /**
     * Extract field errors from ConstraintViolationException.
     *
     * @param ex the constraint violation exception
     * @return list of field errors
     */
    public static List<ErrorResponse.FieldError> extractConstraintViolations(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                .stream()
                .map(violation -> ErrorResponse.FieldError.builder()
                        .field(extractFieldName(violation))
                        .rejectedValue(violation.getInvalidValue())
                        .message(violation.getMessage())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Extract the field name from a constraint violation property path.
     *
     * @param violation the constraint violation
     * @return the leaf field name
     */
    public static String extractFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }

    /**
     * Extract language from a header value.
     *
     * @param acceptLanguageHeader the Accept-Language header value (may be null)
     * @param defaultLang default language if header is absent or blank
     * @return language code
     */
    public static String extractLanguage(String acceptLanguageHeader, String defaultLang) {
        return (acceptLanguageHeader != null && !acceptLanguageHeader.isBlank())
                ? acceptLanguageHeader : defaultLang;
    }
}
