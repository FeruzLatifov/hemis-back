package uz.hemis.api.legacy.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Legacy Entity Adapter - CUBA Platform Compatibility Layer
 *
 * Converts between CUBA Platform API format and modern DTOs.
 * Maintains 100% backward compatibility with old-HEMIS CUBA REST API.
 *
 * @since 2.0.0
 */
@Component
@Slf4j
public class LegacyEntityAdapter {

    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    // CUBA Platform format: "yyyy-MM-dd HH:mm:ss.SSS" (space instead of T)
    private static final DateTimeFormatter CUBA_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * CUBA _local view - excludes underscore-prefixed fields (references)
     * Only includes "local" entity fields without relations
     */
    private static final String VIEW_LOCAL = "_local";

    /**
     * Convert DTO to CUBA Map format with view support
     */
    public Map<String, Object> toMap(Object dto, String entityName, Boolean returnNulls) {
        return toMap(dto, entityName, returnNulls, null);
    }

    /**
     * Convert DTO to CUBA Map format with view filtering
     *
     * @param dto The DTO object to convert
     * @param entityName CUBA entity name (e.g., "hemishe_EStudent")
     * @param returnNulls Whether to include null values
     * @param view CUBA view name (_local excludes underscore-prefixed fields)
     */
    public Map<String, Object> toMap(Object dto, String entityName, Boolean returnNulls, String view) {
        if (dto == null) return null;

        // Check if _local view - exclude underscore-prefixed reference fields
        boolean isLocalView = VIEW_LOCAL.equals(view);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", entityName);
        map.put("_instanceName", getInstanceName(dto));

        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            // Skip static fields (like serialVersionUID)
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(dto);

                // Get JSON property name from @JsonProperty annotation, fallback to field name
                String jsonName = getJsonPropertyName(field);

                // For _local view: skip underscore-prefixed fields (references like _university, _faculty, etc.)
                // These fields are entity references, not local scalar fields
                if (isLocalView && jsonName.startsWith("_")) {
                    continue;
                }

                // For _local view: skip version and fullname fields (not in OLD-hemis _local response)
                if (isLocalView && ("version".equals(jsonName) || "fullname".equals(jsonName))) {
                    continue;
                }

                if (value == null) {
                    if (Boolean.TRUE.equals(returnNulls)) {
                        map.put(jsonName, null);
                    }
                    continue;
                }

                if (value instanceof LocalDateTime) {
                    map.put(jsonName, ((LocalDateTime) value).format(CUBA_DATETIME_FORMAT));
                } else if (value instanceof LocalDate) {
                    map.put(jsonName, ((LocalDate) value).format(ISO_DATE_FORMAT));
                } else if (value instanceof UUID) {
                    map.put(jsonName, value.toString());
                } else if (value instanceof Enum) {
                    map.put(jsonName, ((Enum<?>) value).name());
                } else {
                    map.put(jsonName, value);
                }

            } catch (IllegalAccessException e) {
                log.warn("Cannot access field: {}", field.getName());
            }
        }

        // Add computed fields from methods with @JsonProperty(access = READ_ONLY)
        // Skip for _local view
        if (!isLocalView) {
            addComputedFields(dto, map, returnNulls);
        }

        return map;
    }

    /**
     * Add computed fields from getter methods annotated with @JsonProperty(access = READ_ONLY)
     * These are fields like 'fullname' that are computed, not stored
     */
    private void addComputedFields(Object dto, Map<String, Object> map, Boolean returnNulls) {
        for (java.lang.reflect.Method method : dto.getClass().getMethods()) {
            JsonProperty jsonProperty = method.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && jsonProperty.access() == JsonProperty.Access.READ_ONLY) {
                try {
                    Object value = method.invoke(dto);
                    String jsonName = jsonProperty.value();
                    if (!jsonName.isEmpty()) {
                        if (value != null) {
                            map.put(jsonName, value);
                        } else if (Boolean.TRUE.equals(returnNulls)) {
                            map.put(jsonName, null);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Cannot invoke computed method: {}", method.getName());
                }
            }
        }
    }

    public <T> T fromMap(Map<String, Object> map, Class<T> dtoClass) {
        if (map == null || map.isEmpty()) return null;

        try {
            T dto = dtoClass.getDeclaredConstructor().newInstance();
            
            Map<String, Object> cleanMap = new HashMap<>(map);
            cleanMap.remove("_entityName");
            cleanMap.remove("_instanceName");
            
            for (Field field : dtoClass.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                
                if (!cleanMap.containsKey(fieldName)) continue;
                
                Object value = cleanMap.get(fieldName);
                if (value == null) continue;
                
                Object convertedValue = convertValue(value, field.getType());
                field.set(dto, convertedValue);
            }
            
            return dto;
            
        } catch (Exception e) {
            log.error("Error converting map to DTO: {}", dtoClass.getName(), e);
            throw new RuntimeException("Failed to convert CUBA map to DTO", e);
        }
    }

    private String getInstanceName(Object dto) {
        try {
            // First try to call getFullname() method (for StudentDto)
            try {
                java.lang.reflect.Method fullnameMethod = dto.getClass().getMethod("getFullname");
                Object result = fullnameMethod.invoke(dto);
                if (result != null && !result.toString().isEmpty()) {
                    return result.toString();
                }
            } catch (NoSuchMethodException ignored) {
                // Method doesn't exist, try fallback
            }

            // Fallback: try field-based approach
            Field nameField = findField(dto.getClass(), "name", "fullName", "firstName");
            Field idField = findField(dto.getClass(), "studentIdNumber", "employeeIdNumber", "code");

            StringBuilder name = new StringBuilder();

            if (nameField != null) {
                nameField.setAccessible(true);
                Object val = nameField.get(dto);
                if (val != null) name.append(val);
            }

            if (idField != null) {
                idField.setAccessible(true);
                Object val = idField.get(dto);
                if (val != null) {
                    if (name.length() > 0) name.append(" - ");
                    name.append(val);
                }
            }

            return name.length() > 0 ? name.toString() : dto.toString();

        } catch (Exception e) {
            return dto.toString();
        }
    }

    private Field findField(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // continue
            }
        }
        return null;
    }

    /**
     * Get JSON property name from @JsonProperty annotation, fallback to field name
     *
     * @param field the field to get JSON name from
     * @return JSON property name or field name if no annotation
     */
    private String getJsonPropertyName(Field field) {
        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
        if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
            return jsonProperty.value();
        }
        return field.getName();
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) return value;

        if (targetType == UUID.class && value instanceof String) {
            return UUID.fromString((String) value);
        }

        if (targetType == LocalDateTime.class && value instanceof String) {
            String str = (String) value;
            // Support both CUBA format (space) and ISO format (T)
            if (str.contains("T")) {
                return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            return LocalDateTime.parse(str, CUBA_DATETIME_FORMAT);
        }

        if (targetType == LocalDate.class && value instanceof String) {
            return LocalDate.parse((String) value, ISO_DATE_FORMAT);
        }

        if (targetType.isEnum() && value instanceof String) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Enum enumValue = Enum.valueOf((Class<Enum>) targetType, (String) value);
            return enumValue;
        }

        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            if (value instanceof String) return Integer.parseInt((String) value);
        }

        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
            if (value instanceof String) return Long.parseLong((String) value);
        }

        if ((targetType == Boolean.class || targetType == boolean.class) && value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        return value;
    }

    public List<Map<String, Object>> toMapList(List<?> dtos, String entityName, Boolean returnNulls) {
        return toMapList(dtos, entityName, returnNulls, null);
    }

    public List<Map<String, Object>> toMapList(List<?> dtos, String entityName, Boolean returnNulls, String view) {
        if (dtos == null) return Collections.emptyList();

        List<Map<String, Object>> maps = new ArrayList<>(dtos.size());
        for (Object dto : dtos) {
            maps.add(toMap(dto, entityName, returnNulls, view));
        }
        return maps;
    }
}
