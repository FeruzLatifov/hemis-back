package uz.hemis.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uz.hemis.common.vo.Tin;

public class TinValidator implements ConstraintValidator<ValidTin, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return Tin.isValid(value);
    }
}
