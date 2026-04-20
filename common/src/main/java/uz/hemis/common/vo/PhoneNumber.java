package uz.hemis.common.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Phone number (canonical E.164-like format for Uzbekistan).
 *
 * <p>Accepts input in common formats and normalizes to canonical {@code +998XXXXXXXXX}.</p>
 *
 * <p>Accepted input variants:</p>
 * <ul>
 *   <li>{@code +998901234567} → {@code +998901234567}</li>
 *   <li>{@code 998901234567} → {@code +998901234567}</li>
 *   <li>{@code 8901234567}   → {@code +998901234567}</li>
 *   <li>{@code 901234567}    → {@code +998901234567}</li>
 *   <li>Numbers with spaces/dashes/parens are stripped before validation.</li>
 * </ul>
 *
 * @param value canonical form {@code +998XXXXXXXXX}
 * @since 2.0.0
 */
public record PhoneNumber(@JsonValue String value) {

    private static final Pattern CANONICAL = Pattern.compile("^\\+998\\d{9}$");

    public PhoneNumber {
        Objects.requireNonNull(value, "Phone value must not be null");
        if (!CANONICAL.matcher(value).matches()) {
            throw new IllegalArgumentException("Phone number not in canonical form (+998XXXXXXXXX): " + value);
        }
    }

    /**
     * Parse and normalize free-form phone input to canonical form.
     * Jackson ushbu method'ni string'dan deserialize qilishda ishlatadi.
     *
     * @throws IllegalArgumentException if the number cannot be normalized
     */
    @JsonCreator
    public static PhoneNumber parse(String raw) {
        Objects.requireNonNull(raw, "Phone raw input must not be null");
        String digits = raw.replaceAll("[^\\d+]", "");

        if (digits.startsWith("+998") && digits.length() == 13) {
            return new PhoneNumber(digits);
        }
        if (digits.startsWith("998") && digits.length() == 12) {
            return new PhoneNumber("+" + digits);
        }
        if (digits.startsWith("8") && digits.length() == 10) {
            return new PhoneNumber("+998" + digits.substring(1));
        }
        if (digits.length() == 9 && digits.matches("\\d{9}")) {
            return new PhoneNumber("+998" + digits);
        }
        throw new IllegalArgumentException("Unrecognized phone format: " + raw);
    }

    public static boolean isValid(String raw) {
        try {
            parse(raw);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
