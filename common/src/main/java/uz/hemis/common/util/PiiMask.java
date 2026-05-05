package uz.hemis.common.util;

/**
 * Personally Identifiable Information (PII) maskirovka helperi — log va audit uchun.
 *
 * <p>Loyihada PII (telefon, email, ism) hech qachon log'da plain ko'rinishda chiqmasligi shart
 * (security/CLAUDE.md, OWASP A09 — Logging Failures). Ushbu utility shu joylarda chaqirilib,
 * caller'ga {@code null}/{@code blank} bo'sh string berishi mumkin.</p>
 *
 * <p><strong>PINFL uchun:</strong> {@link uz.hemis.common.vo.Pinfl#maskOrEmpty(String)} ishlating.</p>
 *
 * @since 2.1.0
 */
public final class PiiMask {

    private PiiMask() {}

    /**
     * Telefon raqamini mask qiladi: oxirgi 4 raqam ochiq, kamida 1 ta yulduz, prefix max 4 ta belgi.
     *
     * <ul>
     *   <li>{@code +998901234567} → {@code +998*****4567}</li>
     *   <li>{@code 901234567}     → {@code 9012*4567}</li>
     *   <li>{@code null}          → {@code "null"}</li>
     *   <li>{@code ""}            → {@code "(blank)"}</li>
     * </ul>
     */
    public static String phone(String raw) {
        if (raw == null) return "null";
        if (raw.isBlank()) return "(blank)";
        int len = raw.length();
        if (len <= 4) return "****";
        int prefix = Math.min(4, len - 5);
        if (prefix < 0) prefix = 0;
        int starsCount = Math.max(1, len - prefix - 4);
        return raw.substring(0, prefix) + "*".repeat(starsCount) + raw.substring(len - 4);
    }

    /**
     * Email manzilni mask qiladi: birinchi belgi + {@code ***} + domen.
     *
     * <ul>
     *   <li>{@code john.doe@example.com} → {@code j***@example.com}</li>
     *   <li>{@code a@b.c}                → {@code a***@b.c}</li>
     *   <li>{@code "abc"} (no @)         → {@code "(invalid)"}</li>
     *   <li>{@code null}                 → {@code "null"}</li>
     *   <li>{@code ""}                   → {@code "(blank)"}</li>
     * </ul>
     */
    public static String email(String raw) {
        if (raw == null) return "null";
        if (raw.isBlank()) return "(blank)";
        int at = raw.indexOf('@');
        if (at <= 0 || at == raw.length() - 1) return "(invalid)";
        return raw.charAt(0) + "***" + raw.substring(at);
    }

    /**
     * Generic name maskirovkasi: birinchi belgi ko'rinadi, qolgani {@code *}.
     *
     * <ul>
     *   <li>{@code "Aliyev Ali"} → {@code "A*********"}</li>
     *   <li>{@code "A"}          → {@code "*"}</li>
     *   <li>{@code null}         → {@code "null"}</li>
     * </ul>
     */
    public static String name(String raw) {
        if (raw == null) return "null";
        if (raw.isBlank()) return "(blank)";
        if (raw.length() == 1) return "*";
        return raw.charAt(0) + "*".repeat(raw.length() - 1);
    }
}
