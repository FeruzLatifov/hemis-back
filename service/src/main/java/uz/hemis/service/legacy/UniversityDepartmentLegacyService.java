package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.JsonNull;
import uz.hemis.domain.entity.UniversityDepartment;
import uz.hemis.domain.repository.UniversityDepartmentRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * University Department Legacy Service - Business logic for CUBA department entity endpoints
 *
 * Extracted from UniversityDepartmentEntityController to separate concerns.
 * Handles nested object loading for department views.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UniversityDepartmentLegacyService {

    private final UniversityDepartmentRepository departmentRepository;
    private final CubaNestedObjectLoader nestedLoader;

    private static final String ENTITY_NAME = "hemishe_EUniversityDepartment";

    // ==================== CRUD Operations ====================

    public Optional<UniversityDepartment> findByCode(String code) {
        return departmentRepository.findByCode(code);
    }

    public Optional<UniversityDepartment> findByCodeIncludingDeleted(String code) {
        return departmentRepository.findByCodeIncludingDeleted(code);
    }

    public List<UniversityDepartment> findAll() {
        return departmentRepository.findAll();
    }

    public Page<UniversityDepartment> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    public List<UniversityDepartment> findByUniversityCode(String universityCode) {
        return departmentRepository.findByUniversityCode(universityCode);
    }

    public Page<UniversityDepartment> findByUniversityCode(String universityCode, Pageable pageable) {
        return departmentRepository.findByUniversityCode(universityCode, pageable);
    }

    @Transactional
    public UniversityDepartment save(UniversityDepartment entity) {
        return departmentRepository.save(entity);
    }

    @Transactional
    public UniversityDepartment saveAndFlush(UniversityDepartment entity) {
        return departmentRepository.saveAndFlush(entity);
    }

    /**
     * Create or restore a soft-deleted department — ALL in single transaction.
     *
     * <p>CRITICAL: findByCodeIncludingDeleted + save MUST be in the SAME transaction.
     * Otherwise @SQLRestriction("delete_ts IS NULL") causes merge() to fail
     * on soft-deleted entities (it can't find the row → INSERT → PK violation).</p>
     *
     * @param newEntity entity populated from request body
     * @param universityCode authenticated user's university code
     * @param username authenticated user's username
     * @return saved entity
     */
    @Transactional
    public UniversityDepartment createOrRestore(UniversityDepartment newEntity, String universityCode, String username) {
        Optional<UniversityDepartment> existingOpt = departmentRepository.findByCodeIncludingDeleted(newEntity.getCode());

        if (existingOpt.isPresent()) {
            UniversityDepartment existing = existingOpt.get();

            if (universityCode != null && !universityCode.equals(existing.getUniversityCode())) {
                throw new SecurityException("Department " + newEntity.getCode() + " belongs to university " + existing.getUniversityCode());
            }

            if (existing.getDeleteTs() != null) {
                log.info("Entity was soft-deleted, restoring: {}", newEntity.getCode());
                existing.setDeleteTs(null);
                existing.setDeletedBy(null);
            } else {
                log.info("Entity exists, updating: {}", newEntity.getCode());
            }

            if (newEntity.getNameUz() != null) existing.setNameUz(newEntity.getNameUz());
            if (newEntity.getNameRu() != null) existing.setNameRu(newEntity.getNameRu());
            if (newEntity.getStatus() != null) existing.setStatus(newEntity.getStatus());
            if (newEntity.getDepartmentType() != null) existing.setDepartmentType(newEntity.getDepartmentType());
            if (newEntity.getParentCode() != null) existing.setParentCode(newEntity.getParentCode());
            if (newEntity.getPath() != null) existing.setPath(newEntity.getPath());

            existing.setUpdateTs(LocalDateTime.now());
            existing.setUpdatedBy(username);

            return departmentRepository.saveAndFlush(existing);
        } else {
            newEntity.setUniversityCode(universityCode);
            newEntity.setCreatedBy(username);

            log.info("Creating new entity: code={}, nameUz={}, universityCode={}, departmentType={}",
                    newEntity.getCode(), newEntity.getNameUz(), newEntity.getUniversityCode(), newEntity.getDepartmentType());

            return departmentRepository.saveAndFlush(newEntity);
        }
    }

    @Transactional
    public void softDelete(UniversityDepartment entity, String deletedBy) {
        entity.setDeleteTs(LocalDateTime.now());
        entity.setDeletedBy(deletedBy);
        departmentRepository.save(entity);
    }

    public boolean existsByCode(String code) {
        return departmentRepository.findByCode(code).isPresent();
    }

    // ==================== Mapping Operations ====================

    /**
     * UniversityDepartment entity ni CUBA Map formatiga o'tkazish
     * OLD-HEMIS field order: _entityName, _instanceName, id, parent, university, deparmentType, nameUz, path, nameRu, status
     */
    public Map<String, Object> toDepartmentMap(UniversityDepartment entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        String instanceName = entity.getNameUz() != null ? entity.getNameUz() : entity.getCode();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getCode());
        map.put("code", entity.getCode());

        if (view != null && !"_local".equals(view)) {
            // OLD-HEMIS view mode: parent comes before university
            // Using JsonNull.INSTANCE for null values to ensure they are serialized
            if (entity.getParentCode() != null) {
                Map<String, Object> parentObj = new LinkedHashMap<>();
                parentObj.put("_entityName", ENTITY_NAME);
                parentObj.put("id", entity.getParentCode());
                parentObj.put("code", entity.getParentCode());
                map.put("parent", parentObj);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("parent", JsonNull.INSTANCE);
            }

            Map<String, Object> uniObj = loadUniversityObject(entity.getUniversityCode());
            if (uniObj != null) {
                map.put("university", uniObj);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", JsonNull.INSTANCE);
            }

            if (entity.getDepartmentType() != null) {
                // OLD-HEMIS compatibility: deparmentType _instanceName is name only (no code prefix)
                map.put("deparmentType", nestedLoader.loadClassifierNameOnly(
                    "hemishe_h_university_department_type", "HUniversityDepartmentType", entity.getDepartmentType()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("deparmentType", JsonNull.INSTANCE);
            }

            // OLD-HEMIS field order: nameUz, path, nameRu, status
            // Using JsonNull.INSTANCE for null values
            map.put("nameUz", entity.getNameUz() != null ? entity.getNameUz() : (Boolean.TRUE.equals(returnNulls) ? JsonNull.INSTANCE : null));
            map.put("path", entity.getPath() != null ? entity.getPath() : (Boolean.TRUE.equals(returnNulls) ? JsonNull.INSTANCE : null));
            map.put("nameRu", entity.getNameRu() != null ? entity.getNameRu() : (Boolean.TRUE.equals(returnNulls) ? JsonNull.INSTANCE : null));
            map.put("status", entity.getStatus());
        } else {
            // _local view: only local scalar fields (no code/version — CUBA system fields)
            CubaEntityMapHelper.putIfNotNull(map, "nameUz", entity.getNameUz(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
            CubaEntityMapHelper.putIfNotNull(map, "status", entity.getStatus(), returnNulls);
        }

        return map;
    }

    /**
     * Minimal response format - POST/PUT uchun
     */
    public Map<String, Object> minimalDepartmentResponse(UniversityDepartment entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        String instanceName = entity.getNameUz() != null ? entity.getNameUz() : entity.getCode();
        map.put("_instanceName", instanceName);
        map.put("id", entity.getCode());
        return map;
    }

    /**
     * Map dan entity ga qiymatlarni o'tkazish
     */
    @SuppressWarnings("unchecked")
    public void updateDepartmentFromMap(UniversityDepartment entity, Map<String, Object> map) {
        if (map.containsKey("id")) {
            entity.setCode(String.valueOf(map.get("id")));
        }

        if (map.containsKey("nameUz")) {
            entity.setNameUz((String) map.get("nameUz"));
        }
        if (map.containsKey("nameRu")) {
            entity.setNameRu((String) map.get("nameRu"));
        }

        if (map.containsKey("status")) {
            Object status = map.get("status");
            if (status instanceof Boolean) {
                entity.setStatus((Boolean) status);
            } else if (status instanceof Number) {
                // PHP sends 1/0 as Integer
                entity.setStatus(((Number) status).intValue() != 0);
            } else if (status instanceof String) {
                entity.setStatus("true".equalsIgnoreCase((String) status) || "1".equals(status));
            }
        }

        if (map.containsKey("path")) {
            entity.setPath((String) map.get("path"));
        }

        if (map.containsKey("parent")) {
            Object parent = map.get("parent");
            if (parent instanceof Map) {
                Map<String, Object> parentMap = (Map<String, Object>) parent;
                if (parentMap.containsKey("code")) {
                    entity.setParentCode((String) parentMap.get("code"));
                } else if (parentMap.containsKey("id")) {
                    entity.setParentCode(String.valueOf(parentMap.get("id")));
                }
            } else if (parent instanceof String) {
                entity.setParentCode((String) parent);
            }
        }

        if (map.containsKey("university")) {
            Object university = map.get("university");
            if (university instanceof Map) {
                Map<String, Object> uniMap = (Map<String, Object>) university;
                if (uniMap.containsKey("code")) {
                    entity.setUniversityCode((String) uniMap.get("code"));
                }
            } else if (university instanceof String) {
                entity.setUniversityCode((String) university);
            }
        }

        if (map.containsKey("deparmentType")) {
            Object deptType = map.get("deparmentType");
            if (deptType instanceof Map) {
                Map<String, Object> typeMap = (Map<String, Object>) deptType;
                if (typeMap.containsKey("code")) {
                    entity.setDepartmentType((String) typeMap.get("code"));
                }
            } else if (deptType instanceof String) {
                entity.setDepartmentType((String) deptType);
            }
        }
    }

    // ==================== CUBA Filter Support ====================

    /**
     * URL-encoded JSON string dan filter Map ga parse qilish
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseFilterFromString(String filterStr) {
        if (filterStr == null || filterStr.isEmpty()) {
            return null;
        }
        try {
            String decoded = java.net.URLDecoder.decode(filterStr, java.nio.charset.StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(decoded, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse filter string: {}", filterStr, e);
            return null;
        }
    }

    /**
     * CUBA filter conditions ni entity listga qo'llash
     */
    @SuppressWarnings("unchecked")
    public List<UniversityDepartment> applyFilter(List<UniversityDepartment> entities, Map<String, Object> filterBody) {
        if (filterBody == null || filterBody.isEmpty()) {
            return entities;
        }

        Object filterObj = filterBody.get("filter");
        if (filterObj == null) {
            return entities;
        }

        Map<String, Object> filter;
        if (filterObj instanceof Map) {
            filter = (Map<String, Object>) filterObj;
        } else {
            return entities;
        }

        Object conditionsObj = filter.get("conditions");
        if (conditionsObj == null || !(conditionsObj instanceof List)) {
            return entities;
        }

        List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;
        if (conditions.isEmpty()) {
            return entities;
        }

        return entities.stream()
                .filter(entity -> matchesAllConditions(entity, conditions))
                .collect(Collectors.toList());
    }

    private boolean matchesAllConditions(UniversityDepartment entity, List<Map<String, Object>> conditions) {
        for (Map<String, Object> condition : conditions) {
            if (!matchesCondition(entity, condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCondition(UniversityDepartment entity, Map<String, Object> condition) {
        String property = (String) condition.get("property");
        String operator = (String) condition.get("operator");
        Object value = condition.get("value");

        if (property == null || operator == null) {
            return true;
        }

        if ("university.code".equals(property) || "universityCode".equals(property)) {
            // Apply university.code filter for security isolation
            String universityCode = value != null ? value.toString() : null;
            if (universityCode != null && !universityCode.isEmpty()) {
                return universityCode.equals(entity.getUniversityCode());
            }
            return true;
        }

        Object entityValue = getPropertyValue(entity, property);
        return evaluateCondition(entityValue, operator, value);
    }

    private Object getPropertyValue(UniversityDepartment entity, String property) {
        return switch (property) {
            case "code", "id" -> entity.getCode();
            case "nameUz" -> entity.getNameUz();
            case "nameRu" -> entity.getNameRu();
            case "name" -> entity.getNameUz();
            case "status" -> entity.getStatus();
            case "path" -> entity.getPath();
            case "university.code", "universityCode" -> entity.getUniversityCode();
            case "deparmentType.code", "departmentType.code", "departmentType" -> entity.getDepartmentType();
            case "parent.code", "parentCode" -> entity.getParentCode();
            default -> {
                log.warn("Unknown property: {}", property);
                yield null;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Object entityValue, String operator, Object filterValue) {
        return switch (operator.toLowerCase()) {
            case "=" -> {
                if (entityValue == null && filterValue == null) yield true;
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().equals(filterValue.toString());
            }
            case "<>", "!=" -> {
                if (entityValue == null && filterValue == null) yield false;
                if (entityValue == null || filterValue == null) yield true;
                yield !entityValue.toString().equals(filterValue.toString());
            }
            case "like", "contains" -> {
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().toLowerCase().contains(filterValue.toString().toLowerCase());
            }
            case "startswith" -> {
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().toLowerCase().startsWith(filterValue.toString().toLowerCase());
            }
            case "endswith" -> {
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().toLowerCase().endsWith(filterValue.toString().toLowerCase());
            }
            case "in" -> {
                if (entityValue == null || filterValue == null) yield false;
                if (filterValue instanceof List) {
                    List<Object> values = (List<Object>) filterValue;
                    yield values.stream().anyMatch(v -> entityValue.toString().equals(v.toString()));
                }
                yield false;
            }
            case "isnull" -> entityValue == null;
            case "notnull" -> entityValue != null;
            case ">" -> compareValues(entityValue, filterValue) > 0;
            case "<" -> compareValues(entityValue, filterValue) < 0;
            case ">=" -> compareValues(entityValue, filterValue) >= 0;
            case "<=" -> compareValues(entityValue, filterValue) <= 0;
            default -> {
                log.warn("Unknown operator: {}", operator);
                yield true;
            }
        };
    }

    private int compareValues(Object entityValue, Object filterValue) {
        if (entityValue == null || filterValue == null) {
            return 0;
        }
        try {
            double d1 = Double.parseDouble(entityValue.toString());
            double d2 = Double.parseDouble(filterValue.toString());
            return Double.compare(d1, d2);
        } catch (NumberFormatException e) {
            return entityValue.toString().compareTo(filterValue.toString());
        }
    }

    // ==================== Utility Methods ====================

    /**
     * CUBA-compatible 404 Not Found xato response
     */
    public Map<String, Object> cubaNotFoundError(String entityId) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Entity not found");
        error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
        return error;
    }

    /**
     * String qiymatni Boolean ga parse qilish
     */
    public Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty() || "undefined".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return "true".equalsIgnoreCase(value);
    }

    // ==================== Nested Object Loading ====================

    /**
     * Load university full object for department view mode
     *
     * @param universityCode university code
     * @return CUBA-format university map or null
     */
    public Map<String, Object> loadUniversityObject(String universityCode) {
        return nestedLoader.loadUniversityFull(universityCode);
    }

    /**
     * Load classifier object for department type, etc.
     *
     * @param tableName DB table name
     * @param entityName CUBA entity name suffix
     * @param code classifier code
     * @return CUBA-format classifier map or null
     */
    public Map<String, Object> loadClassifierObject(String tableName, String entityName, String code) {
        return nestedLoader.loadClassifier(tableName, entityName, code);
    }

    /**
     * Load classifier with additional name fields (name_ru, name_en)
     */
    public Map<String, Object> loadClassifierObjectWithNames(String tableName, String entityName, String code) {
        return nestedLoader.loadClassifierWithNames(tableName, entityName, code);
    }

    /**
     * Load SOATO object
     */
    public Map<String, Object> loadSoatoObject(String soatoCode) {
        return nestedLoader.loadSoato(soatoCode);
    }
}
