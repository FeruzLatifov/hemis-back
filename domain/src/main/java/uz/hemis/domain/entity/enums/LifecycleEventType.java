package uz.hemis.domain.entity.enums;

/**
 * University lifecycle event type — matches DB CHECK constraint on {@code university_lifecycle.event_type}.
 *
 * <p>Stored as {@link jakarta.persistence.EnumType#STRING}. Each value here mirrors a
 * frontend {@code STATUS_EVENT_MAP} entry — adding a new value without a matching
 * UI trigger leaves a dead enum branch.</p>
 */
public enum LifecycleEventType {
    /** Universitet yopilgan. */
    CLOSED,
    /** Boshqa universitetga qo'shilgan (successor required). */
    MERGED,
    /** Litsenziya bekor qilingan. */
    LICENSE_REVOKED,
    /** Faoliyat vaqtincha to'xtatilgan. */
    SUSPENDED,
    /** Qayta faollashtirilgan. */
    REACTIVATED,
    /** Qayta tashkil etilgan (successor required). */
    REORGANIZED
}
