package uz.hemis.domain.entity.enums;

/**
 * University lifecycle event type — matches DB CHECK constraint on {@code university_lifecycle.event_type}.
 *
 * <p>Stored as {@link jakarta.persistence.EnumType#STRING}.</p>
 */
public enum LifecycleEventType {
    /** Universitet yopilgan. */
    CLOSED,
    /** Boshqa universitetga qo'shilgan. */
    MERGED,
    /** Bir necha universitetga bo'lingan. */
    SPLIT,
    /** Litsenziya bekor qilingan. */
    LICENSE_REVOKED,
    /** Faoliyat vaqtincha to'xtatilgan. */
    SUSPENDED,
    /** Qayta faollashtirilgan. */
    REACTIVATED,
    /** Nomi o'zgartirilgan. */
    RENAMED,
    /** Qayta tashkil etilgan. */
    REORGANIZED
}
