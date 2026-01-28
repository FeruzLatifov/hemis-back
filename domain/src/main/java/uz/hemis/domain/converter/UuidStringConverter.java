package uz.hemis.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/**
 * UUID <-> String converter for PostgreSQL compatibility.
 *
 * PostgreSQL JDBC driver sometimes returns UUID columns as String.
 * This converter handles both cases:
 * - Reading: String -> UUID
 * - Writing: UUID -> String (for proper null handling)
 */
@Converter
public class UuidStringConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return UUID.fromString(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
