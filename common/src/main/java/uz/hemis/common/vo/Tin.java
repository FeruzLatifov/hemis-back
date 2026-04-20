package uz.hemis.common.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * TIN — Tax Identification Number (STIR) for legal entities in Uzbekistan.
 *
 * <p>9-digit numeric identifier. Format: {@code DDDDDDDDD}.</p>
 *
 * <p>Immutable, validated at construction.</p>
 *
 * @param value raw 9-digit string
 * @since 2.0.0
 */
public record Tin(@JsonValue String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\d{9}$");

    public Tin {
        Objects.requireNonNull(value, "TIN value must not be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid TIN format (expected 9 digits): " + value);
        }
    }

    @JsonCreator
    public static Tin of(String value) {
        return new Tin(value);
    }

    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
