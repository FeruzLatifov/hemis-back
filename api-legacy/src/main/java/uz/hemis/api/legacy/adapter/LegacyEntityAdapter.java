package uz.hemis.api.legacy.adapter;

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
    private static final DateTimeFormatter ISO_DATETIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Map<String, Object> toMap(Object dto, String entityName, Boolean returnNulls) {
        if (dto == null) return null;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", entityName);
        map.put("_instanceName", getInstanceName(dto));
        
        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);
                
                if (value == null) {
                    if (Boolean.TRUE.equals(returnNulls)) {
                        map.put(field.getName(), null);
                    }
                    continue;
                }
                
                if (value instanceof LocalDateTime) {
                    map.put(field.getName(), ((LocalDateTime) value).format(ISO_DATETIME_FORMAT));
                } else if (value instanceof LocalDate) {
                    map.put(field.getName(), ((LocalDate) value).format(ISO_DATE_FORMAT));
                } else if (value instanceof UUID) {
                    map.put(field.getName(), value.toString());
                } else if (value instanceof Enum) {
                    map.put(field.getName(), ((Enum<?>) value).name());
                } else {
                    map.put(field.getName(), value);
                }
                
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field: {}", field.getName());
            }
        }
        
        return map;
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

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) return value;

        if (targetType == UUID.class && value instanceof String) {
            return UUID.fromString((String) value);
        }

        if (targetType == LocalDateTime.class && value instanceof String) {
            return LocalDateTime.parse((String) value, ISO_DATETIME_FORMAT);
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
        if (dtos == null) return Collections.emptyList();

        List<Map<String, Object>> maps = new ArrayList<>(dtos.size());
        for (Object dto : dtos) {
            maps.add(toMap(dto, entityName, returnNulls));
        }
        return maps;
    }
}
