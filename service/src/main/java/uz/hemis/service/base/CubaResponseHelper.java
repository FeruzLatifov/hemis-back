package uz.hemis.service.base;

import uz.hemis.common.vo.Pinfl;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for common CUBA service response building, validation, and pagination.
 *
 * <p>Provides static helper methods used by internal CUBA-compatible services
 * (TeacherCubaService, StudentCubaService, etc.).</p>
 *
 * <p><strong>IMPORTANT:</strong></p>
 * <ul>
 *   <li>This is INTERNAL optimization only</li>
 *   <li>External API (URLs, request/response format) unchanged</li>
 *   <li>100% backward compatible with OLD-HEMIS CUBA pattern</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Slf4j
public final class CubaResponseHelper {

    private CubaResponseHelper() {
        // Utility class — no instantiation
    }

    // =====================================================
    // Response Builders
    // =====================================================

    /**
     * Build success response with data
     *
     * @param data Data to include in response
     * @return Success response map
     */
    public static Map<String, Object> successResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * Build success response with list
     *
     * @param items List of items
     * @param count Total count
     * @return Success response map
     */
    public static Map<String, Object> successListResponse(List<?> items, int count) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", items);
        response.put("count", count);
        return response;
    }

    /**
     * Build success response with list (count = size)
     *
     * @param items List of items
     * @return Success response map
     */
    public static Map<String, Object> successListResponse(List<?> items) {
        return successListResponse(items, items.size());
    }

    /**
     * Build error response
     *
     * @param code    Error code (e.g., "not_found", "invalid_parameter")
     * @param message Error message
     * @return Error response map
     */
    public static Map<String, Object> errorResponse(String code, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }

    /**
     * Build not found error response
     *
     * @param entityName Entity name (e.g., "Faculty", "Student")
     * @return Error response map
     */
    public static Map<String, Object> notFoundResponse(String entityName) {
        return errorResponse("not_found", entityName + " not found");
    }

    // =====================================================
    // Entity Conversion
    // =====================================================

    /**
     * Convert list of entities to list of maps
     *
     * @param entities  List of entities
     * @param converter Converter function (entity → map)
     * @param <T>       Entity type
     * @return List of maps
     */
    public static <T> List<Map<String, Object>> entitiesToMaps(List<T> entities,
                                                               Function<T, Map<String, Object>> converter) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    /**
     * Build simple entity map (id, code, name)
     *
     * @param id   Entity ID
     * @param code Entity code
     * @param name Entity name
     * @return Entity map
     */
    public static Map<String, Object> buildSimpleEntityMap(UUID id, String code, String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("code", code);
        map.put("name", name);
        return map;
    }

    /**
     * Build entity map with additional fields
     *
     * @param id               Entity ID
     * @param code             Entity code
     * @param name             Entity name
     * @param additionalFields Additional fields to add
     * @return Entity map
     */
    public static Map<String, Object> buildEntityMap(UUID id, String code, String name,
                                                     Map<String, Object> additionalFields) {
        Map<String, Object> map = buildSimpleEntityMap(id, code, name);
        if (additionalFields != null) {
            map.putAll(additionalFields);
        }
        return map;
    }

    // =====================================================
    // Input Validation Methods
    // =====================================================

    /**
     * Validate required parameter
     *
     * @param paramName  Parameter name
     * @param paramValue Parameter value
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validateRequired(String paramName, String paramValue) {
        if (paramValue == null || paramValue.trim().isEmpty()) {
            log.warn("Required parameter missing: {}", paramName);
            return errorResponse("invalid_parameter", "Required parameter: " + paramName);
        }
        return null;
    }

    /**
     * Validate PINFL format (14 digits)
     *
     * @param pinfl PINFL to validate
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validatePinfl(String pinfl) {
        if (pinfl == null || pinfl.trim().isEmpty()) {
            log.warn("PINFL is required");
            return errorResponse("invalid_pinfl", "PINFL is required");
        }

        String trimmedPinfl = pinfl.trim();

        if (!trimmedPinfl.matches("\\d{14}")) {
            log.warn("Invalid PINFL format: {}", Pinfl.maskOrEmpty(pinfl));
            return errorResponse("invalid_pinfl",
                    "PINFL must be exactly 14 digits. Example: 12345678901234");
        }

        return null;
    }

    /**
     * Validate date format (yyyy-MM-dd)
     *
     * @param date      Date string
     * @param paramName Parameter name for error message
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validateDate(String date, String paramName) {
        if (date == null || date.trim().isEmpty()) {
            return errorResponse("invalid_date", paramName + " is required");
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            log.warn("Invalid date format: {}", date);
            return errorResponse("invalid_date",
                    paramName + " must be in format yyyy-MM-dd. Example: 2024-01-15");
        }

        return null;
    }

    /**
     * Validate year format (yyyy)
     *
     * @param year Year string
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validateYear(String year) {
        if (year == null || year.trim().isEmpty()) {
            return errorResponse("invalid_year", "Year is required");
        }

        if (!year.matches("\\d{4}")) {
            log.warn("Invalid year format: {}", year);
            return errorResponse("invalid_year",
                    "Year must be 4 digits. Example: 2024");
        }

        int yearValue = Integer.parseInt(year);
        if (yearValue < 1900 || yearValue > 2100) {
            return errorResponse("invalid_year",
                    "Year must be between 1900 and 2100");
        }

        return null;
    }

    /**
     * Validate email format
     *
     * @param email Email address
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return errorResponse("invalid_email", "Email is required");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            log.warn("Invalid email format: {}", email);
            return errorResponse("invalid_email",
                    "Invalid email format. Example: user@example.com");
        }

        return null;
    }

    /**
     * Validate phone number format (Uzbekistan)
     *
     * @param phone Phone number
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return errorResponse("invalid_phone", "Phone number is required");
        }

        String phoneRegex = "^(\\+?998)?\\d{9}$";
        if (!phone.replaceAll("[-\\s]", "").matches(phoneRegex)) {
            log.warn("Invalid phone format: {}", phone);
            return errorResponse("invalid_phone",
                    "Invalid phone number format. Example: +998901234567");
        }

        return null;
    }

    /**
     * Validate university code format
     *
     * @param universityCode University code
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validateUniversityCode(String universityCode) {
        if (universityCode == null || universityCode.trim().isEmpty()) {
            return errorResponse("invalid_university", "University code is required");
        }

        if (!universityCode.matches("\\d{5}")) {
            log.warn("Invalid university code format: {}", universityCode);
            return errorResponse("invalid_university",
                    "University code must be 5 digits. Example: 00123");
        }

        return null;
    }

    /**
     * Validate numeric parameter
     *
     * @param value     Value to validate
     * @param paramName Parameter name for error message
     * @param min       Minimum value (inclusive, null = no min)
     * @param max       Maximum value (inclusive, null = no max)
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validateNumeric(String value, String paramName,
                                                      Integer min, Integer max) {
        if (value == null || value.trim().isEmpty()) {
            return errorResponse("invalid_parameter", paramName + " is required");
        }

        try {
            int numValue = Integer.parseInt(value);

            if (min != null && numValue < min) {
                return errorResponse("invalid_parameter",
                        paramName + " must be at least " + min);
            }

            if (max != null && numValue > max) {
                return errorResponse("invalid_parameter",
                        paramName + " must be at most " + max);
            }

            return null;
        } catch (NumberFormatException e) {
            log.warn("Invalid numeric format for {}: {}", paramName, value);
            return errorResponse("invalid_parameter",
                    paramName + " must be a valid number");
        }
    }

    /**
     * Validate string length
     *
     * @param value     Value to validate
     * @param paramName Parameter name
     * @param minLength Minimum length (null = no min)
     * @param maxLength Maximum length (null = no max)
     * @return Error response if invalid, null if valid
     */
    public static Map<String, Object> validateLength(String value, String paramName,
                                                     Integer minLength, Integer maxLength) {
        if (value == null) {
            return errorResponse("invalid_parameter", paramName + " is required");
        }

        int length = value.length();

        if (minLength != null && length < minLength) {
            return errorResponse("invalid_parameter",
                    paramName + " must be at least " + minLength + " characters");
        }

        if (maxLength != null && length > maxLength) {
            return errorResponse("invalid_parameter",
                    paramName + " must be at most " + maxLength + " characters");
        }

        return null;
    }

    // =====================================================
    // Pagination
    // =====================================================

    /**
     * Apply pagination to list
     *
     * @param items  Full list of items
     * @param limit  Page size (null = no limit)
     * @param offset Page offset (null = 0)
     * @param <T>    Item type
     * @return Paginated list
     */
    public static <T> List<T> paginate(List<T> items, Integer limit, Integer offset) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        int pageOffset = (offset != null && offset >= 0) ? offset : 0;
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, 1000) : items.size();

        return items.stream()
                .skip(pageOffset)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    /**
     * Build paginated response
     *
     * @param allItems       All items (before pagination)
     * @param paginatedItems Paginated items
     * @param limit          Page size
     * @param offset         Page offset
     * @param <T>            Item type
     * @return Paginated response map
     */
    public static <T> Map<String, Object> paginatedResponse(List<T> allItems, List<T> paginatedItems,
                                                            Integer limit, Integer offset) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", paginatedItems);
        response.put("count", paginatedItems.size());
        response.put("total", allItems.size());
        response.put("limit", limit != null ? Math.min(limit, 1000) : allItems.size());
        response.put("offset", offset != null ? offset : 0);
        return response;
    }

    // =====================================================
    // String Utils
    // =====================================================

    /**
     * Safe string trim
     *
     * @param str String to trim
     * @return Trimmed string or null
     */
    public static String safeTrim(String str) {
        return str != null ? str.trim() : null;
    }

    /**
     * Check if string is empty
     *
     * @param str String to check
     * @return true if null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not empty
     *
     * @param str String to check
     * @return true if not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
