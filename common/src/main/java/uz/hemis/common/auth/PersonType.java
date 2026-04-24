package uz.hemis.common.auth;

/**
 * Person type discriminator for the universal {@code employee} registry.
 *
 * <p>HEMIS stores all physical persons in one table — OTM staff, ministry staff,
 * center staff (DTM/UzACI) and external org staff (GUVD, Hokimiyat, ...).
 * The discriminator decides which business context the person belongs to.</p>
 *
 * @since 2.1.0
 */
public enum PersonType {
    /** Universitet xodimi — 46K (mavjud default). */
    UNIVERSITY_STAFF,

    /** Vazirlik (Oliy va o'rta maxsus ta'lim) xodimi. */
    MINISTRY_STAFF,

    /** Markazlar (DTM, UzACI, va h.k.) xodimlari. */
    CENTER_STAFF,

    /** Boshqa vazirlik yoki davlat tashkiloti (Hokimiyat, GUVD, Tax, BIMM, ...). */
    OTHER_ORG_STAFF
}
