package uz.hemis.api.legacy.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CUBA Platform filter helper for legacy entity controllers.
 *
 * Supports CUBA filter format:
 * {"conditions":[{"property":"fieldName","operator":"=","value":"someValue"}]}
 *
 * Operators:
 * - =, equal - exact match
 * - <>, !=, notEqual - not equal
 * - contains - substring match (case-insensitive)
 * - startsWith - prefix match (case-insensitive)
 * - endsWith - suffix match (case-insensitive)
 * - >, <, >=, <= - numeric comparison
 * - isNull - check if null
 * - notEmpty - check if not null and not empty string
 */
@Component
@Slf4j
public class CubaFilterHelper {

    private final ObjectMapper objectMapper;

    public CubaFilterHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Apply CUBA filter and pagination to entity list
     *
     * @param entities All entities
     * @param filterJson CUBA filter JSON string
     * @param offset Pagination offset
     * @param limit Pagination limit
     * @param propertyGetter Function to get property value from entity
     * @return Filtered and paginated list
     */
    public <T> List<T> applyFilterAndPagination(
            List<T> entities,
            String filterJson,
            int offset,
            int limit,
            Function<PropertyRequest<T>, Object> propertyGetter) {

        List<T> filtered = applyFilter(entities, filterJson, propertyGetter);

        int start = Math.min(offset, filtered.size());
        int end = Math.min(start + limit, filtered.size());

        return filtered.subList(start, end);
    }

    /**
     * Apply only pagination to entity list (without filter)
     *
     * @param entities All entities
     * @param offset Pagination offset
     * @param limit Pagination limit
     * @return Paginated list
     */
    public <T> List<T> applyPagination(List<T> entities, int offset, int limit) {
        int start = Math.min(offset, entities.size());
        int end = Math.min(start + limit, entities.size());
        return entities.subList(start, end);
    }

    /**
     * Apply CUBA filter to entity list
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> applyFilter(
            List<T> entities,
            String filterJson,
            Function<PropertyRequest<T>, Object> propertyGetter) {

        if (filterJson == null || filterJson.isBlank()) {
            log.info("No filter provided, returning all {} entities", entities.size());
            return entities;
        }

        log.info("Applying filter: [{}]", filterJson);

        try {
            Map<String, Object> filter = objectMapper.readValue(filterJson, new TypeReference<>() {});
            Object conditionsObj = filter.get("conditions");
            log.info("Parsed filter map: {}, conditions: {}", filter.keySet(), conditionsObj);

            if (!(conditionsObj instanceof List)) {
                return entities;
            }

            List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

            List<T> result = entities.stream()
                .filter(entity -> matchesAllConditions(entity, conditions, propertyGetter))
                .collect(Collectors.toList());

            log.info("Filter result: {} entities -> {} matches", entities.size(), result.size());
            return result;

        } catch (Exception e) {
            log.warn("Cannot parse CUBA filter: {} - returning all entities", e.getMessage());
            return entities;
        }
    }

    /**
     * Parse filter from request body (POST /search)
     */
    public String extractFilterFromBody(Map<String, Object> body) {
        if (body == null) {
            return null;
        }

        try {
            // Filter field in body
            if (body.containsKey("filter")) {
                Object filterObj = body.get("filter");
                if (filterObj instanceof String) {
                    return (String) filterObj;
                } else if (filterObj instanceof Map) {
                    return objectMapper.writeValueAsString(filterObj);
                }
            }
            // Body itself is filter (has conditions)
            if (body.containsKey("conditions")) {
                return objectMapper.writeValueAsString(body);
            }
        } catch (Exception e) {
            log.warn("Cannot serialize filter: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract integer from body or use default
     */
    public int extractInt(Map<String, Object> body, String key, Integer queryParam, int defaultValue) {
        if (queryParam != null) {
            return queryParam;
        }
        if (body != null && body.containsKey(key)) {
            Object value = body.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return defaultValue;
    }

    private <T> boolean matchesAllConditions(
            T entity,
            List<Map<String, Object>> conditions,
            Function<PropertyRequest<T>, Object> propertyGetter) {

        for (Map<String, Object> condition : conditions) {
            if (!matchesCondition(entity, condition, propertyGetter)) {
                return false;
            }
        }
        return true;
    }

    private <T> boolean matchesCondition(
            T entity,
            Map<String, Object> condition,
            Function<PropertyRequest<T>, Object> propertyGetter) {

        String property = (String) condition.get("property");
        String operator = (String) condition.get("operator");
        Object filterValue = condition.get("value");

        if (property == null || operator == null) {
            log.debug("Missing property or operator in condition: {}", condition);
            return true;
        }

        Object entityValue = propertyGetter.apply(new PropertyRequest<>(entity, property));
        boolean matches = compareValues(entityValue, operator, filterValue);
        if (!matches) {
            log.debug("Filter mismatch: {}={} {} {} -> false", property, entityValue, operator, filterValue);
        }

        return matches;
    }

    private boolean compareValues(Object entityValue, String operator, Object filterValue) {
        // Handle null checks for special operators
        if ("isNull".equals(operator)) {
            return entityValue == null;
        }
        if ("notEmpty".equals(operator)) {
            if (entityValue == null) return false;
            if (entityValue instanceof String s) return !s.isEmpty();
            return true;
        }

        // For other operators, if filter value is null, skip
        if (filterValue == null) {
            return true;
        }

        String entityStr = entityValue != null ? entityValue.toString() : "";
        String filterStr = filterValue.toString();

        return switch (operator) {
            case "=", "equal" -> entityStr.equals(filterStr);
            case "<>", "!=", "notEqual" -> !entityStr.equals(filterStr);
            case "contains" -> entityStr.toLowerCase().contains(filterStr.toLowerCase());
            case "startsWith" -> entityStr.toLowerCase().startsWith(filterStr.toLowerCase());
            case "endsWith" -> entityStr.toLowerCase().endsWith(filterStr.toLowerCase());
            case ">" -> compareNumeric(entityStr, filterStr) > 0;
            case "<" -> compareNumeric(entityStr, filterStr) < 0;
            case ">=" -> compareNumeric(entityStr, filterStr) >= 0;
            case "<=" -> compareNumeric(entityStr, filterStr) <= 0;
            default -> true;
        };
    }

    private int compareNumeric(String a, String b) {
        try {
            return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }

    /**
     * Get property value using reflection (fallback)
     */
    public Object getPropertyByReflection(Object entity, String property) {
        try {
            // Remove underscore prefix if present
            String cleanProperty = property.startsWith("_") ? property.substring(1) : property;

            // Try getter method
            String getterName = "get" + Character.toUpperCase(cleanProperty.charAt(0)) + cleanProperty.substring(1);
            Method getter = entity.getClass().getMethod(getterName);
            Object value = getter.invoke(entity);

            log.trace("Got property {} (getter: {}) = {}", property, getterName, value);
            return value;
        } catch (Exception e) {
            log.debug("Cannot get property {} from {}: {}", property, entity.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Request object for property getter function
     */
    public record PropertyRequest<T>(T entity, String property) {}
}
