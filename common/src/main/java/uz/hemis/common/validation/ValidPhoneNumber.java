package uz.hemis.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean Validation constraint: field must be a phone number parseable to canonical {@code +998XXXXXXXXX}.
 *
 * <p>Null values are allowed.</p>
 *
 * @since 2.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number (UZ +998XXXXXXXXX)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
