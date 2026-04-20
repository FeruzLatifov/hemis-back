package uz.hemis.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uz.hemis.common.vo.Pinfl;

/**
 * JPA converter: {@link Pinfl} ↔ {@code VARCHAR} (raw 14-digit string).
 *
 * <p>DB column shape o'zgarmaydi ({@code VARCHAR(14)}). Entity field tipi {@code Pinfl} —
 * type-safe. Null-safe: NULL column → NULL VO, NULL VO → NULL column.</p>
 *
 * <p>{@code autoApply=false} — converter'ni qo'l bilan {@code @Convert(converter = ...)}
 * bilan qo'llash kerak. Bu — Pinfl bo'lmagan VARCHAR columnlarni buzib qo'ymaslik uchun.</p>
 */
@Converter(autoApply = false)
public class PinflConverter implements AttributeConverter<Pinfl, String> {

    @Override
    public String convertToDatabaseColumn(Pinfl attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Pinfl convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        // DB'dagi eski yozuvlar noto'g'ri format'da bo'lishi mumkin — log va null
        // qaytarish o'rniga exception — bu yaroqsiz ma'lumotni ochiq-oydin ko'rsatadi.
        return Pinfl.of(dbData);
    }
}
