package uz.hemis.api.university.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.hemis.common.error.ApiProblem;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;

/**
 * University API Exception Handler — RFC 7807 Problem Details.
 *
 * <p>Scope: {@code uz.hemis.api.university.controller} paketi.</p>
 * <p>Response: {@code Content-Type: application/problem+json}</p>
 *
 * @since 2.0.0
 */
@RestControllerAdvice(basePackages = "uz.hemis.api.university.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class UniversityExceptionHandler {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String TYPE_PREFIX = "https://hemis.uz/errors/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiProblem> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("University API resource not found: {}", ex.getMessage());
        return problem(404, "Resource Not Found", "resource-not-found", "NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiProblem> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        log.warn("University API bad request: {}", ex.getMessage());
        return problem(400, "Bad Request", "bad-request", "BAD_REQUEST", ex.getMessage(), req);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiProblem> handleValidation(ValidationException ex, HttpServletRequest req) {
        log.warn("University API validation error: {}", ex.getMessage());
        return problem(422, "Validation Failed", "validation-error", "VALIDATION_ERROR", ex.getMessage(), req);
    }

    private ResponseEntity<ApiProblem> problem(int status, String title, String typeSlug,
                                                String code, String detail, HttpServletRequest req) {
        ApiProblem body = ApiProblem.builder()
                .type(TYPE_PREFIX + typeSlug)
                .title(title)
                .status(status)
                .code(code)
                .detail(detail)
                .instance(req.getRequestURI())
                .traceId(MDC.get("traceId"))
                .build();
        return ResponseEntity.status(status)
                .contentType(MediaType.valueOf(PROBLEM_JSON))
                .body(body);
    }
}
