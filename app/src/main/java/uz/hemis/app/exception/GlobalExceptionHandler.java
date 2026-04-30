package uz.hemis.app.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.common.audit.AuditContext;
import uz.hemis.common.audit.ErrorEvent;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.common.exception.BadRequestException;
import uz.hemis.common.exception.ExceptionHandlerUtils;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;

import java.util.*;
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
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${hemis.audit.enabled:false}")
    private boolean auditEnabled;

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

    /**
     * Handle IllegalArgumentException
     *
     * <p>HTTP Status: 400 BAD REQUEST</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.error("Illegal argument: {}", ex.getMessage());

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

        List<ErrorResponse.FieldError> fieldErrors = ExceptionHandlerUtils.extractFieldErrors(ex);

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

        List<ErrorResponse.FieldError> fieldErrors = ExceptionHandlerUtils.extractConstraintViolations(ex);

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
     * <p>HTTP Status: 400 BAD REQUEST (or 500 for legacy endpoints)</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.error("Malformed JSON request: {}", ex.getMessage());

        // OLD-HEMIS format for legacy endpoints
        if (isLegacyEndpoint(request)) {
            java.util.Map<String, String> legacyError = new java.util.LinkedHashMap<>();
            legacyError.put("error", "Server error");
            legacyError.put("details", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(legacyError);
        }

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON",
                "Request body contains invalid JSON",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle HttpMediaTypeNotSupportedException
     *
     * <p>Thrown when Content-Type is not supported by the endpoint</p>
     * <p>HTTP Status: 415 UNSUPPORTED MEDIA TYPE</p>
     *
     * @param ex exception
     * @param request HTTP request
     * @return error response
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("Unsupported media type: {} for {}", ex.getContentType(), request.getRequestURI());

        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String message = String.format(
                "Content-Type '%s' is not supported. Supported types: %s",
                ex.getContentType(),
                supportedTypes.isEmpty() ? "application/json" : supportedTypes
        );

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Unsupported Media Type",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
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
    public ResponseEntity<?> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // Publish ErrorEvent for audit logging
        publishErrorEvent(ex, request);

        // OLD-HEMIS format for legacy endpoints
        if (isLegacyEndpoint(request)) {
            java.util.Map<String, String> legacyError = new java.util.LinkedHashMap<>();
            legacyError.put("error", "Server error");
            legacyError.put("details", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(legacyError);
        }

        // Capture to Sentry (auto-captures if enabled)
        Object sentryId = Sentry.captureException(ex);
        String eventId = sentryId != null ? sentryId.toString() : null;

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
     * Map missing static resources (e.g., /swagger-ui) to 404 instead of 500.
     * Legacy CUBA endpoints uchun CUBA formatida 404 qaytaradi.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();

        // Legacy CUBA endpointlar uchun CUBA formatida qaytarish
        if (isLegacyEndpoint(request)) {
            Map<String, Object> cubaError = new LinkedHashMap<>();

            if (path.contains("/services/")) {
                String servicePart = path.substring(path.indexOf("/services/") + "/services/".length());
                String[] parts = servicePart.split("/", 2);
                String service = parts.length > 0 ? parts[0] : "unknown";
                String method = parts.length > 1 ? parts[1].split("/")[0] : "unknown";
                cubaError.put("error", "Service method not found");
                cubaError.put("details", service + "." + method + "()");
            } else if (path.contains("/entities/")) {
                String entityPart = path.substring(path.indexOf("/entities/") + "/entities/".length());
                String entity = entityPart.split("/")[0];
                cubaError.put("error", "MetaClass not found");
                cubaError.put("details", "MetaClass " + entity + " not found");
            } else {
                cubaError.put("error", "Not found");
                cubaError.put("details", path);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cubaError);
        }

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
     * Publish ErrorEvent for audit logging (500 errors only)
     */
    private void publishErrorEvent(Exception ex, HttpServletRequest request) {
        if (!auditEnabled) return;
        try {
            // Stack trace 16000 belgigacha — root cause uzun chain'larda kesilmasligi uchun
            java.io.StringWriter sw = new java.io.StringWriter();
            ex.printStackTrace(new java.io.PrintWriter(sw));
            String stackTrace = sw.toString();
            if (stackTrace.length() > 16000) {
                stackTrace = stackTrace.substring(0, 16000);
            }

            String clientIp = MDC.get("clientIp");

            // User context — kim xato qilganini aniqlash
            AuditContext.AuditContextBuilder ctxBuilder = AuditContext.builder()
                    .ip(clientIp != null ? clientIp : request.getRemoteAddr())
                    .requestId(MDC.get("requestId"))
                    .endpoint(request.getRequestURI());

            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
                    org.springframework.security.oauth2.jwt.Jwt jwt = jwtAuth.getToken();
                    String sub = jwt.getSubject();
                    if (sub != null) {
                        try {
                            ctxBuilder.userId(java.util.UUID.fromString(sub));
                        } catch (IllegalArgumentException ignored) {
                            ctxBuilder.username(sub);
                        }
                    }
                    String usernameClaim = jwt.getClaimAsString("username");
                    if (usernameClaim != null && !usernameClaim.isBlank()) {
                        ctxBuilder.username(usernameClaim);
                    }
                } else {
                    String name = auth.getName();
                    if (name != null) {
                        try {
                            ctxBuilder.userId(java.util.UUID.fromString(name));
                        } catch (IllegalArgumentException ignored) {
                            ctxBuilder.username(name);
                        }
                    }
                }
            }

            eventPublisher.publishEvent(ErrorEvent.builder()
                    .context(ctxBuilder.build())
                    .errorType(ex.getClass().getSimpleName())
                    .errorMessage(ex.getMessage())
                    .stackTrace(stackTrace)
                    .endpoint(request.getMethod() + " " + request.getRequestURI())
                    .requestBody(extractRequestBody(request))
                    .build());
        } catch (Exception e) {
            log.warn("Failed to publish error audit event: {}", e.getMessage());
        }
    }

    /**
     * Cached body'dan request payload'ni JSON Map sifatida o'qish.
     * AuditRequestFilter ContentCachingRequestWrapper bilan o'rab qo'ygan bo'lsa ishlaydi.
     * AuditRepository.toJson() darajasida sezgir maydonlar (password) niqoblanadi.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractRequestBody(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) return null;
        byte[] bytes = wrapper.getContentAsByteArray();
        if (bytes == null || bytes.length == 0) return null;
        try {
            return objectMapper.readValue(bytes, Map.class);
        } catch (Exception e) {
            // JSON emas yoki noto'g'ri formatda — raw matn sifatida saqlash
            String raw = new String(bytes, StandardCharsets.UTF_8);
            return Map.of("_raw", raw.length() > 500 ? raw.substring(0, 500) + "..." : raw);
        }
    }

    /**
     * Check if request is for legacy CUBA endpoints
     *
     * @param request HTTP request
     * @return true if legacy endpoint
     */
    private boolean isLegacyEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && (
            uri.startsWith("/app/rest/v2/") ||
            uri.startsWith("/rest/v2/")
        );
    }
}
