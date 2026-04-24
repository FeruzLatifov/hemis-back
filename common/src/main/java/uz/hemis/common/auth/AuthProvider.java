package uz.hemis.common.auth;

/**
 * Primary authentication provider for {@code users} (human logins).
 *
 * <p>Supports HEMIS integrations declared in {@code .claude/context.md}:
 * MyGov SSO, OneID, E-Imzo (ERI kalit), Mobile-ID. Default — username+password.</p>
 *
 * @since 2.1.0
 */
public enum AuthProvider {
    /** Oddiy username + password (BCrypt). */
    PASSWORD,

    /** MyGov.uz portal orqali SSO (PINFL callback). */
    MYGOV,

    /** sso.egov.uz OneID — Uzbekistan government SSO. */
    ONEID,

    /** E-Imzo (ERI kalit) — PKCS#7 certificate'dan PINFL. */
    E_IMZO,

    /** Mobile ID (telefon + MyGov) — kelajak. */
    MOBILE_ID
}
