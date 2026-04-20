package uz.hemis.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uz.hemis.common.vo.Tin;

/**
 * JPA converter: {@link Tin} ↔ {@code VARCHAR} (raw 9-digit string).
 *
 * <p>Null-safe. {@code autoApply=false} — qo'l bilan {@code @Convert(converter = ...)}.</p>
 */
@Converter(autoApply = false)
public class TinConverter implements AttributeConverter<Tin, String> {

    @Override
    public String convertToDatabaseColumn(Tin attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Tin convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return Tin.of(dbData);
    }
}
