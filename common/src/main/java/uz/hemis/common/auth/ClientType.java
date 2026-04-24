package uz.hemis.common.auth;

/**
 * Client type discriminator for {@code oauth_client} (B2B machine accounts).
 *
 * <p>OAuth 2.0 RFC 6749 — client_credentials grant type. Machine-to-machine
 * authentication alohida jadvalda (users'dan ajratilgan) chunki lifecycle,
 * policy va audit talablari farqli.</p>
 *
 * @since 2.1.0
 */
public enum ClientType {
    /** 224 universitet backend (univer.php) — B2B sync. */
    UNIVERSITY_BACKEND,

    /** Tashqi davlat sistemasi (MyGov, OneID, Hokimiyat, GUVD, Tax, ...). */
    EXTERNAL_SYSTEM,

    /** HEMIS ichki service (analytics, email, notifications, ...). */
    INTERNAL_SERVICE
}
