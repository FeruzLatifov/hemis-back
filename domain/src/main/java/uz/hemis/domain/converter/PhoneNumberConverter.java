package uz.hemis.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uz.hemis.common.vo.PhoneNumber;

/**
 * JPA converter: {@link PhoneNumber} ↔ {@code VARCHAR} (canonical +998 format).
 *
 * <p>DB'da canonical format saqlanadi ({@code +998XXXXXXXXX}). Eski yozuvlar
 * free-form bo'lsa, {@link PhoneNumber#parse(String)} normalize qilishga urinadi.</p>
 *
 * <p>Null-safe. {@code autoApply=false}.</p>
 */
@Converter(autoApply = false)
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {

    @Override
    public String convertToDatabaseColumn(PhoneNumber attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        // DB eski yozuvlarida free-form bo'lsa, parse() normalize qiladi
        return PhoneNumber.parse(dbData);
    }
}
