package uz.hemis.common;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Marker object representing JSON null that bypasses Jackson's NON_NULL filter.
 *
 * When placed in a Map as a value, Jackson's NON_NULL inclusion setting
 * won't exclude this entry (because the value is a non-null object reference).
 * The {@code @JsonValue} method returns null, which Jackson serializes as JSON null.
 *
 * Usage:
 * <pre>
 * map.put("field", JsonNull.INSTANCE);
 * // Jackson serializes as: "field": null
 * </pre>
 *
 * @since 2.0.0
 */
public final class JsonNull {

    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    @JsonValue
    public Object toJson() {
        return null;
    }
}
