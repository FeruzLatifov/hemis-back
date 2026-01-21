package uz.hemis.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

/**
 * Converter for handling UUID columns that PostgreSQL JDBC driver returns as String.
 * This converter allows storing UUID in database but reading it as String in Java.
 */
@Converter
public class UuidStringConverter implements AttributeConverter<String, UUID> {

    @Override
    public UUID convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(attribute);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String convertToEntityAttribute(UUID dbData) {
        return dbData != null ? dbData.toString() : null;

import java.util.UUID;

/**
 * UUID <-> String converter for PostgreSQL compatibility.
 *
 * PostgreSQL JDBC driver sometimes returns UUID columns as String.
 * This converter handles both cases:
 * - Reading: String/UUID -> UUID
 * - Writing: UUID -> UUID (JDBC driver handles the rest)
 */
@Converter
public class UuidStringConverter implements AttributeConverter<UUID, Object> {

    @Override
    public Object convertToDatabaseColumn(UUID uuid) {
        return uuid; // Return UUID object - JDBC driver will cast to uuid type
    }

    @Override
    public UUID convertToEntityAttribute(Object dbData) {
        if (dbData == null) return null;
        if (dbData instanceof UUID) return (UUID) dbData;
        if (dbData instanceof String) {
            String str = (String) dbData;
            if (str.isEmpty()) return null;
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
