package uz.hemis.service.legacy;

import uz.hemis.common.JsonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Shared utility methods for CUBA-compatible entity ↔ Map conversions.
 * Replaces duplicate extractXxx / getXxxValue / putIfNotNull helpers
 * that previously lived in every legacy entity controller.
 *
 * All methods are static – the class is not meant to be instantiated.
 *
 * @since 2.1.0
 */
public final class CubaEntityMapHelper {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private CubaEntityMapHelper() {
        // utility class
    }

    // ------------------------------------------------------------------
    //  Extract helpers  (request body → typed value)
    // ------------------------------------------------------------------

    /**
     * Extracts a UUID from a value that may be:
     * <ul>
     *   <li>a {@link UUID} instance</li>
     *   <li>a plain UUID string</li>
     *   <li>a CUBA nested-object map: {@code {"id": "uuid-string"}}</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public static UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof String str && !str.isEmpty()) {
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id instanceof String str && !str.isEmpty()) {
                try {
                    return UUID.fromString(str);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Extracts a string value from various CUBA formats:
     * <ul>
     *   <li>Plain string: {@code "401"}</li>
     *   <li>Nested object with id: {@code {"id": "401"}}</li>
     *   <li>Nested object with code: {@code {"code": "401"}}</li>
     *   <li>Any other object via toString()</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public static String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof String str) return str;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) return extractString(id);
            Object code = nested.get("code");
            if (code != null) return extractString(code);
        }
        return value.toString();
    }

    /**
     * Extracts the "id" field from a CUBA nested-object map.
     * Returns null for non-Map values (unlike {@link #extractString} which falls back to toString).
     */
    @SuppressWarnings("unchecked")
    public static String extractStringId(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
    }

    /**
     * Extracts a code-based reference value. Tries "code" first, then "id".
     * Returns null for non-Map values.
     */
    @SuppressWarnings("unchecked")
    public static String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            if (code != null) return code.toString();
            Object id = nested.get("id");
            if (id != null) return id.toString();
        }
        return null;
    }

    /**
     * Extracts a code or id from various CUBA formats.
     * Handles plain strings and nested objects {"id":"..."} or {"code":"..."}.
     * Returns the string value or null.
     */
    @SuppressWarnings("unchecked")
    public static String extractCodeOrId(Object value) {
        if (value == null) return null;
        if (value instanceof String str) {
            return str.isEmpty() ? null : str;
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            if (code != null) return code.toString();
            Object id = nested.get("id");
            if (id != null) return id.toString();
        }
        return value.toString();
    }

    // ------------------------------------------------------------------
    //  Simple type converters
    // ------------------------------------------------------------------

    public static String getStringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    public static Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double getDoubleValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean getBooleanValue(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(value.toString());
    }

    public static BigDecimal getBigDecimalValue(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static LocalDate parseDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        try {
            return LocalDate.parse(value.toString(), DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Alias for parseDate - parses LocalDate from various formats.
     */
    public static LocalDate parseLocalDate(Object value) {
        return parseDate(value);
    }

    public static LocalDateTime parseLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof String str) {
            if (str.isEmpty()) return null;
            try {
                return LocalDateTime.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    //  Map-put helper
    // ------------------------------------------------------------------

    /**
     * Puts the value into the map if it is non-null, or if {@code returnNulls} is true.
     */
    public static void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            // OLD-HEMIS: Jackson NON_NULL global config skips null values,
            // but CUBA returnNulls=true requires null fields in response.
            // JsonNull.INSTANCE serializes as JSON null while bypassing NON_NULL filter.
            map.put(key, JsonNull.INSTANCE);
        }
    }
}
