package uz.hemis.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import uz.hemis.domain.entity.enums.PermissionAction;

/**
 * JPA converter: {@link PermissionAction} enum ↔ lowercase DB column.
 *
 * <p>DB stores action as lowercase (e.g. {@code "view"}) per the {@code chk_permission_action}
 * CHECK constraint. Java enum uses UPPERCASE per Java convention.</p>
 *
 * <p><strong>Unknown values are tolerated, not fatal.</strong> The DB constraint and this enum are
 * two copies of the same list, and a migration necessarily lands before the JAR that knows the new
 * verb — {@code M015} added {@code 'restore'} exactly that way. A strict {@code valueOf} would then
 * throw while Hibernate materialises a {@code Permission} row, and because permissions are loaded
 * as a set for the whole user, one unreadable row failed the entire login: every holder of that
 * permission — SUPER_ADMIN and ADMIN included — locked out for the length of the rollout window,
 * and again after any image rollback. Returning {@code null} instead keeps the row usable
 * ({@code Permission.getCode()} is what authorization actually reads; {@code isWritePermission()}
 * treats a null action as non-write, i.e. the safe side) and leaves a warning in the log naming the
 * verb to add to the enum.</p>
 */
@Slf4j
@Converter(autoApply = false)
public class PermissionActionConverter implements AttributeConverter<PermissionAction, String> {

    @Override
    public String convertToDatabaseColumn(PermissionAction value) {
        return value == null ? null : value.name().toLowerCase();
    }

    @Override
    public PermissionAction convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            return PermissionAction.valueOf(dbValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown permission.action '{}' in DB — not in PermissionAction enum. "
                    + "Add it to the enum (and to isWritePermission if it mutates state); "
                    + "treating it as null so permission loading and login still work.", dbValue);
            return null;
        }
    }
}
