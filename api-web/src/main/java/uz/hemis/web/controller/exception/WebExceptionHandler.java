package uz.hemis.web.controller.exception;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.hemis.common.dto.ErrorResponse;
import uz.hemis.common.exception.ExceptionHandlerUtils;
import uz.hemis.service.shared.I18nService;

import java.util.stream.Collectors;

/**
 * Web API Exception Handler — api-web module controller'lari uchun.
 *
 * <p>Mapping:
 * <ul>
 *   <li>Authentication fail → 401 Unauthorized (localized)</li>
 *   <li>OptimisticLock → 409 Conflict (concurrent edit)</li>
 *   <li>DataIntegrityViolation → 400 Bad Request (FK/CHECK/UNIQUE)</li>
 *   <li>Validation fail → 400 Bad Request (field-level details)</li>
 * </ul></p>
 *
 * <p>AccessDeniedException, RuntimeException kabi umumiy xatolar
 * GlobalExceptionHandler tomonidan boshqariladi.</p>
 */
@RestControllerAdvice(basePackages = "uz.hemis.web.controller")
@RequiredArgsConstructor
@Slf4j
public class WebExceptionHandler {

    private static final String DEFAULT_LANGUAGE = "uz-UZ";

    private final I18nService i18nService;

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

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Optimistic lock — concurrent update conflict. Client retry kerak.
     */
    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            Exception ex,
            HttpServletRequest request
    ) {
        log.warn("Optimistic lock conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "Ma'lumot boshqa foydalanuvchi tomonidan o'zgartirildi. Sahifani yangilab qayta urining.",
                request.getRequestURI(),
                null,
                "OPTIMISTIC_LOCK_CONFLICT"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * FK/CHECK/UNIQUE violation — DB-level validation fail.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String rootMessage = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), rootMessage);
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Ma'lumot yaxlitligi buzildi: " + simplifyDbError(rootMessage),
                request.getRequestURI(),
                null,
                "DATA_INTEGRITY_VIOLATION"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * @Valid body validation — field-level xato tafsilotlari.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.info("Validation failed at {}: {}", request.getRequestURI(), details);
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validatsiya xatosi: " + details,
                request.getRequestURI(),
                null,
                "VALIDATION_FAILED"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Path variable / query param / method-level @Validated xatosi.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.info("Constraint violation at {}: {}", request.getRequestURI(), details);
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Constraint xatosi: " + details,
                request.getRequestURI(),
                null,
                "CONSTRAINT_VIOLATION"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * DB error'ni foydalanuvchi o'qiy oladigan qisqa matn'ga o'zgartirish.
     */
    private String simplifyDbError(String raw) {
        if (raw == null) return "Noma'lum DB xato";
        if (raw.contains("is not present in table")) {
            return "Bog'langan yozuv topilmadi (FK)";
        }
        if (raw.contains("duplicate key")) {
            return "Takroriy qiymat — allaqachon mavjud";
        }
        if (raw.contains("violates check constraint")) {
            return "Qiymat ruxsat etilgan oralig'idan tashqarida";
        }
        if (raw.contains("violates not-null")) {
            return "Majburiy maydon bo'sh";
        }
        return raw.length() > 200 ? raw.substring(0, 200) + "..." : raw;
    }
}
