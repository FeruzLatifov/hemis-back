package uz.hemis.common.audit;

/**
 * Audit action types for activity logging.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    VIEW,
    EXPORT,
    IMPORT,
    /** Soft-delete undo — its own action so a restore is not read as an ordinary edit. */
    RESTORE
}
