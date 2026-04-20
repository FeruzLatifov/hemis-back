package uz.hemis.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean Validation constraint: field must be a valid 14-digit PINFL.
 *
 * <p>Null values are allowed — combine with {@code @NotNull} when required.</p>
 *
 * <p>Usage on DTO fields:</p>
 * <pre>
 *   &#064;ValidPinfl
 *   private String pinfl;
 * </pre>
 *
 * @since 2.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PinflValidator.class)
public @interface ValidPinfl {
    String message() default "Invalid PINFL (expected 14 digits)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
