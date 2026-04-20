package uz.hemis.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uz.hemis.domain.entity.enums.PermissionAction;

/**
 * JPA converter: {@link PermissionAction} enum ↔ lowercase DB column.
 *
 * <p>DB stores action as lowercase (e.g. {@code "view"}) per CHECK constraint
 * defined in V003. Java enum uses UPPERCASE per Java convention.</p>
 */
@Converter(autoApply = false)
public class PermissionActionConverter implements AttributeConverter<PermissionAction, String> {

    @Override
    public String convertToDatabaseColumn(PermissionAction value) {
        return value == null ? null : value.name().toLowerCase();
    }

    @Override
    public PermissionAction convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : PermissionAction.valueOf(dbValue.toUpperCase());
    }
}
