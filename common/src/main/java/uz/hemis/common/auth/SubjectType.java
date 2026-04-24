package uz.hemis.common.auth;

/**
 * JWT token subject discriminator.
 *
 * <p>HEMIS har JWT da {@code typ} claim ishlatadi — foydalanuvchi (human) va
 * OAuth client (machine) ni ajratish uchun.</p>
 *
 * <ul>
 *   <li>{@link #USER} — {@code users} jadvalidan login (vazirlik, admin, rektor, ...)</li>
 *   <li>{@link #CLIENT} — {@code oauth_client} jadvalidan (univer B2B, MyGov, OneID, ...)</li>
 * </ul>
 *
 * @since 2.1.0
 */
public enum SubjectType {
    USER,
    CLIENT
}
