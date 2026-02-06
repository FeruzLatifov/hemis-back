package uz.hemis.api.legacy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * CUBA REST API mos keluvchi search body parser.
 *
 * <p>OLD-HEMIS CUBA Platform search/filter so'rovlarini parse qiladi.
 * Offset/limit/filter parametrlarni body dan xavfsiz ajratib oladi.</p>
 *
 * <p>Bu utility controllerlar orasidagi takroriy offset/limit parsing
 * kodini markazlashtiradi.</p>
 *
 * @since 1.5.4
 */
@Slf4j
public final class CubaSearchBodyParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CubaSearchBodyParser() {
        // Utility class
    }

    /**
     * Body dan filter JSON string ni olish.
     *
     * @param body request body map (nullable)
     * @return filter JSON string yoki null
     */
    public static String extractFilter(Map<String, Object> body) {
        if (body == null) return null;
        Object filterObj = body.get("filter");
        if (filterObj == null) return null;

        try {
            return MAPPER.writeValueAsString(filterObj);
        } catch (Exception e) {
            log.debug("Filter serialize xatosi: {}", e.getMessage());
            return filterObj.toString();
        }
    }

    /**
     * Body dan integer qiymat olish (offset yoki limit).
     *
     * <p>Avval Number tekshiriladi, keyin String parse qilinadi.
     * Xatolik bo'lsa default qiymat qaytariladi.</p>
     *
     * @param body         request body map (nullable)
     * @param key          kalit nomi ("offset" yoki "limit")
     * @param queryParam   URL query parameter qiymati (nullable, ustunlik qiladi)
     * @param defaultValue default qiymat
     * @return ajratib olingan integer qiymat
     */
    public static int extractInt(Map<String, Object> body, String key, Integer queryParam, int defaultValue) {
        if (queryParam != null) {
            return queryParam;
        }
        if (body == null || !body.containsKey(key)) {
            return defaultValue;
        }

        Object obj = body.get(key);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj != null) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (NumberFormatException e) {
                log.debug("Body '{}' qiymati integer emas: '{}' — default {} ishlatiladi", key, obj, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Body dan offset olish (bodyOffset null bo'lsa).
     *
     * @param body       request body map (nullable)
     * @param bodyOffset hozirgi body offset qiymati (nullable)
     * @return yangilangan offset qiymati yoki asl bodyOffset
     */
    public static Integer extractOffset(Map<String, Object> body, Integer bodyOffset) {
        if (bodyOffset != null || body == null || !body.containsKey("offset")) {
            return bodyOffset;
        }
        return parseIntSafe(body.get("offset"));
    }

    /**
     * Body dan limit olish (bodyLimit null bo'lsa).
     *
     * @param body      request body map (nullable)
     * @param bodyLimit hozirgi body limit qiymati (nullable)
     * @return yangilangan limit qiymati yoki asl bodyLimit
     */
    public static Integer extractLimit(Map<String, Object> body, Integer bodyLimit) {
        if (bodyLimit != null || body == null || !body.containsKey("limit")) {
            return bodyLimit;
        }
        return parseIntSafe(body.get("limit"));
    }

    /**
     * Object ni Integer ga xavfsiz parse qilish.
     * Number yoki String format qabul qilinadi.
     *
     * @param obj parse qilinadigan ob'yekt
     * @return Integer qiymat yoki null
     */
    private static Integer parseIntSafe(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj != null) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (NumberFormatException e) {
                log.debug("Integer parse xatosi: '{}' — null qaytariladi", obj);
            }
        }
        return null;
    }
}
