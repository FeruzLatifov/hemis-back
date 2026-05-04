package uz.hemis.app.exception;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uz.hemis.common.dto.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring-coupled validation extraction helpers.
 *
 * <p>Lives in the {@code app} module because {@code common} must remain Spring-free
 * (per common/CLAUDE.md). Used by {@link GlobalExceptionHandler} for
 * {@link MethodArgumentNotValidException} translation.</p>
 *
 * <p>Jakarta-only and pure-Java helpers stay in
 * {@code uz.hemis.common.exception.ExceptionHandlerUtils}.</p>
 *
 * @since 2.1.0
 */
public final class SpringValidationUtils {

    private SpringValidationUtils() {
    }

    /**
     * Extract field errors from {@link MethodArgumentNotValidException}.
     *
     * @param ex the validation exception thrown by Spring's {@code @Valid} processing
     * @return ordered list of {@link ErrorResponse.FieldError} entries
     */
    public static List<ErrorResponse.FieldError> extractFieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
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
    }
}
