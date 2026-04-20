package uz.hemis.domain.entity.enums;

/**
 * Permission category — matches DB CHECK constraint on {@code permission.category}.
 *
 * <p>Stored as {@link jakarta.persistence.EnumType#STRING}.</p>
 */
public enum PermissionCategory {
    /** Asosiy biznes-entitylar (students, teachers, ...). */
    CORE,
    /** Administrativ funksiyalar (users, roles). */
    ADMIN,
    /** Menu access permission'lari. */
    MENU,
    /** Foydalanuvchi tomonidan yaratilgan. */
    CUSTOM,
    /** Hisobot funksiyalari. */
    REPORTS
}
