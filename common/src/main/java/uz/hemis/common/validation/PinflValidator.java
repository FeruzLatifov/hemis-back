package uz.hemis.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uz.hemis.common.vo.Pinfl;

/**
 * Validator for {@link ValidPinfl}. Delegates to {@link Pinfl#isValid(String)}.
 */
public class PinflValidator implements ConstraintValidator<ValidPinfl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return Pinfl.isValid(value);
    }
}
