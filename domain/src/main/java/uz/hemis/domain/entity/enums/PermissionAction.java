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
    APPROVE,
    /**
     * Bring a soft-deleted row back (recycle bin). Deliberately separate from {@link #DELETE}: the
     * two carry different risk — one hides a row, the other returns it — so a role can be trusted
     * with only one of them. Added to the {@code chk_permission_action} CHECK by M015; the DB
     * constraint, this enum and {@link uz.hemis.domain.entity.security.Permission#isWritePermission()}
     * must move together, or the converter throws on login for whoever holds the permission.
     */
    RESTORE
}
