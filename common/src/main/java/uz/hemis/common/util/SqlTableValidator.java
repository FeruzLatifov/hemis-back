package uz.hemis.common.util;

import java.util.regex.Pattern;

/**
 * Native SQL table-name validator — defense-in-depth against SQL injection
 * via {@code tableName} string interpolation in legacy classifier lookup.
 *
 * <p><strong>Context:</strong> 175/175 Univer kontrakt'ini xizmat qiluvchi
 * legacy classifier lookup'lar (`hemishe_h_position`, `hemishe_h_gender`, …)
 * dynamic table-name pattern bilan ishlaydi:</p>
 *
 * <pre>
 * jdbcTemplate.queryForObject(
 *     "SELECT code, name FROM " + tableName + " WHERE code = ?",
 *     ...);
 * </pre>
 *
 * <p>Hozirgi callsite'larning hammasi <b>hardcoded literal</b> bilan chaqiriladi
 * (xavfsiz), lekin kelajakda dynamic kelishi va injection xavfini ochishi mumkin.
 * Ushbu validator hardcoded literal'lar uchun ham guard qo'shadi — har callsite
 * {@link #validateLegacyClassifier(String)} ni SQL build'dan oldin chaqiradi.</p>
 *
 * <p><strong>Naming convention (domain/CLAUDE.md):</strong></p>
 * <ul>
 *   <li>{@code hemishe_e_*} — eski CUBA entity (frozen)</li>
 *   <li>{@code hemishe_h_*} — eski CUBA classifier (frozen)</li>
 *   <li>{@code hemishe_r_*} — eski CUBA reference (frozen)</li>
 *   <li>{@code h_*} — yangi classifier (ADR-0006)</li>
 * </ul>
 *
 * <p>Regex shu pattern'larni qatlamlashtirib qabul qiladi.</p>
 *
 * @since P3-1 (audit hardening)
 */
public final class SqlTableValidator {

    /**
     * Allowed table-name pattern: {@code hemishe_[her]_<lowercase_snake>}
     * yoki {@code h_<lowercase_snake>}. Boshqa hech narsa qabul qilinmaydi
     * (jumladan {@code DROP}, ` -- comment`, semikolon, bo'sh joy).
     */
    private static final Pattern SAFE_LEGACY_TABLE =
        Pattern.compile("^(hemishe_[her]_[a-z][a-z0-9_]*|h_[a-z][a-z0-9_]*)$");

    private SqlTableValidator() {
        // utility class
    }

    /**
     * Validate legacy classifier/entity table name.
     *
     * @param tableName non-null table identifier (e.g. {@code hemishe_h_position})
     * @return the same {@code tableName} (for inline use:
     *         {@code "FROM " + SqlTableValidator.validateLegacyClassifier(t)})
     * @throws IllegalArgumentException if name is null, blank, or fails the pattern
     */
    public static String validateLegacyClassifier(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName must not be null or blank");
        }
        if (!SAFE_LEGACY_TABLE.matcher(tableName).matches()) {
            throw new IllegalArgumentException(
                "Unsafe table name rejected: '" + tableName + "' — expected "
                + "hemishe_[h|e|r]_<lower_snake> or h_<lower_snake>"
            );
        }
        return tableName;
    }

    /**
     * Non-throwing variant — useful when callsite logs the bad name instead
     * of failing the request (e.g. when {@code tableName} comes from a stale
     * cache entry).
     *
     * @return true if tableName matches the legacy classifier pattern
     */
    public static boolean isSafeLegacyClassifier(String tableName) {
        return tableName != null && SAFE_LEGACY_TABLE.matcher(tableName).matches();
    }
}
