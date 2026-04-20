package uz.hemis.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean Validation constraint: field must be a valid 9-digit TIN (STIR).
 *
 * <p>Null values are allowed.</p>
 *
 * @since 2.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TinValidator.class)
public @interface ValidTin {
    String message() default "Invalid TIN (expected 9 digits)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
