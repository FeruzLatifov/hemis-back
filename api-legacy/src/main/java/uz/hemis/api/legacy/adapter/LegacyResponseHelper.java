package uz.hemis.api.legacy.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper for building CUBA-compatible error/status response maps.
 *
 * <p><strong>Why:</strong> {@code Map.of(k1, v1, k2, v2)} returns an unordered map for ≥3 entries
 * and even for 2 entries the JSON serialization order is implementation-defined
 * (relies on {@code KeyValueHolder}). CUBA Platform 7.3 clients (Univer Yii2 PHP frontend)
 * require strict insertion order — so every legacy response map MUST be a {@link LinkedHashMap}.</p>
 *
 * <p><strong>Future-proof:</strong> Centralizing in this helper means future format changes
 * (e.g. adding {@code timestamp}) happen in ONE place instead of 25+ controllers.</p>
 *
 * @since 2.1.0
 */
public final class LegacyResponseHelper {

    private LegacyResponseHelper() {
    }

    /**
     * Two-field error response with strict insertion order.
     *
     * @param error short error code (e.g. "Entity not found", "Validation failed")
     * @param details human-readable details
     * @return ordered map: {@code {"error": ..., "details": ...}}
     */
    public static Map<String, Object> errorMap(String error, String details) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", error);
        m.put("details", details);
        return m;
    }

    /**
     * Standard "Entity not found" 404 body matching the api-legacy convention.
     *
     * @param entityName CUBA entity name (e.g. "hemishe_EFaculty")
     * @param id the lookup identifier
     * @return ordered map with error="Entity not found" and standardized details
     */
    public static Map<String, Object> notFoundMap(String entityName, Object id) {
        return errorMap("Entity not found", "Entity " + entityName + " with id " + id + " not found");
    }

    /**
     * Two-field {@code {success: false, message: ...}} body — used by old-hemis
     * StudentService endpoints which signal failure via the {@code success} flag (vs. HTTP status alone).
     *
     * @param message failure description shown to caller
     * @return ordered map: {@code {"success": false, "message": ...}}
     */
    public static Map<String, Object> failureMap(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", false);
        m.put("message", message);
        return m;
    }

    /**
     * Standard 403 Forbidden body for cross-tenant BOLA defense.
     *
     * @return ordered map: {@code {"error": "Forbidden", "details": "Resource belongs to another university"}}
     */
    public static Map<String, Object> forbiddenMap() {
        return errorMap("Forbidden", "Resource belongs to another university");
    }
}
