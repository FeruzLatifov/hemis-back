package uz.hemis.common.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * PINFL (Personal Identification Number for Physical Person).
 *
 * <p>14-digit numeric identifier issued to every citizen of the Republic of Uzbekistan.
 * Format: {@code DDDDDDDDDDDDDD} (14 digits, no separators).</p>
 *
 * <p>Immutable value object — validated at construction. Use {@link #of(String)} to create.</p>
 *
 * <p><strong>Note:</strong> PINFL uniqueness is NOT enforced (historical data has duplicates).</p>
 *
 * @param value raw 14-digit string
 * @since 2.0.0
 */
public record Pinfl(@JsonValue String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\d{14}$");

    public Pinfl {
        Objects.requireNonNull(value, "PINFL value must not be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid PINFL format (expected 14 digits): " + mask(value));
        }
    }

    /** Jackson uchun factory — string'dan avtomatik deserialize. */
    @JsonCreator
    public static Pinfl of(String value) {
        return new Pinfl(value);
    }

    /** Validate without constructing; returns true if valid 14-digit PINFL. */
    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value).matches();
    }

    /** Masked representation for logging: {@code 12345*****1234}. */
    public String masked() {
        return mask(value);
    }

    /**
     * Defensive masking helper — does NOT throw on invalid input.
     * <p>Use in logs / audit when caller may receive any string and PII must never appear in plain.</p>
     *
     * @param raw any string (may be null, blank, malformed)
     * @return masked representation safe for logs
     */
    public static String maskOrEmpty(String raw) {
        if (raw == null) return "null";
        if (raw.isBlank()) return "(blank)";
        return mask(raw);
    }

    private static String mask(String raw) {
        if (raw == null || raw.length() < 9) return "****";
        return raw.substring(0, 5) + "*****" + raw.substring(raw.length() - 4);
    }

    @Override
    public String toString() {
        return masked();
    }
}
