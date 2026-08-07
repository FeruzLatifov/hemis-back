package uz.hemis.domain.entity.enums;

/**
 * Permission action — matches DB CHECK constraint on {@code permission.action}.
 *
 * <p>Stored as lowercase string in DB via {@link uz.hemis.domain.converter.PermissionActionConverter}
 * (e.g. {@code VIEW} → {@code "view"}).</p>
 */
public enum PermissionAction {
    VIEW,
    CREATE,
    EDIT,
    DELETE,
    EXPORT,
    IMPORT,
    MANAGE,
    ACCESS,
    SYNC,
    APPROVE
}
