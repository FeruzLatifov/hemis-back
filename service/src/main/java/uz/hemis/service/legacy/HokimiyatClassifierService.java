package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Hokimiyat Classifier Service - hokimiyat-specific classifier operations.
 *
 * <p>Handles OLD-HEMIS hokimiyat endpoints:</p>
 * <ul>
 *   <li>GET /app/rest/v2/services/classifiers/hokimiyat</li>
 *   <li>GET /app/rest/v2/services/classifiers/hokimiyatInfo</li>
 * </ul>
 *
 * <p>Extracted from {@link ClassifierLegacyService} to reduce class size.</p>
 *
 * @since 2.2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HokimiyatClassifierService {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter CUBA_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * OLD-HEMIS hokimiyat classifiers — exact mapping (20 ta).
     * Key: API response dagi nom (univer tomonidan yuboriladi).
     * Value: Hozirgi DB table nomi — YANGI jadvallarga yo'naltirilgan (Bosqich 3 refactor).
     * h_university — maxsus holat (EUniversity, legacy qoladi).
     */
    private static final Map<String, String> HOKIMIYAT_CLASSIFIER_MAP = new LinkedHashMap<>() {{
        put("h_education_type", "hemishe_h_education_type");
        put("h_education_form", "hemishe_h_education_form");
        put("h_education_year", "hemishe_h_education_year");
        put("h_university", "hemishe_e_university"); // Special: EUniversity table
        put("h_ownership", "hemishe_h_ownership");
        put("h_course", "hemishe_h_course");
        put("h_nationality", "hemishe_h_nationality");
        put("h_citizenship_type", "hemishe_h_citizenship"); // API key differs from table
        put("h_social_category", "hemishe_h_student_social_type"); // API key differs from table
        put("h_gender", "hemishe_h_gender");
        put("h_student_type", "hemishe_h_student_type");
        put("h_payment_form", "hemishe_h_payment_form");
        put("h_stipend_rate", "hemishe_h_stipend_rate");
        put("h_language", "hemishe_h_education_language"); // API key differs from table
        put("h_accommodation", "hemishe_h_accomodation"); // Note: single 'm' in DB
        put("h_student_living_status", "hemishe_h_student_living_status");
        put("h_student_roommate_type", "hemishe_h_student_room_mate_type");
        put("h_student_status", "hemishe_h_student_status_type"); // API key differs from table
        put("h_soato", "hemishe_h_soato");
        put("h_country", "hemishe_h_country");
    }};

    /**
     * Get hokimiyat classifiers (OLD-HEMIS /hokimiyat endpoint)
     * OLD-HEMIS bilan 100% mos - 20 ta classifier
     *
     * <p>Cached 24h: each call performs ~180 JDBC queries (20 classifiers × ~9 introspection
     * + data queries). Reference data only changes via admin classifier edits, which evict
     * the cache through {@link ClassifierLegacyService} mutation methods.</p>
     */
    @Cacheable(value = "hokimiyatClassifiers", key = "'all'", unless = "#result == null")
    public Map<String, Object> getHokimiyatClassifiers() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : HOKIMIYAT_CLASSIFIER_MAP.entrySet()) {
            String apiKey = entry.getKey();
            String tableName = entry.getValue();

            try {
                // Special case for h_university - uses EUniversity table
                if ("h_university".equals(apiKey)) {
                    Map<String, Object> uniData = getUniversityClassifierForHokimiyat();
                    if (uniData != null) {
                        classifiersList.add(uniData);
                    }
                    continue;
                }

                Map<String, Object> classifierData = getClassifierWithItemsForHokimiyat(apiKey, tableName);
                if (classifierData != null) {
                    classifiersList.add(classifierData);
                }
            } catch (Exception e) {
                log.debug("Error loading hokimiyat classifier {}: {}", apiKey, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get hokimiyat classifiers info (metadata only, no items)
     * OLD-HEMIS /hokimiyatInfo endpoint
     *
     * <p>Cached 24h. Same invalidation triggers as {@link #getHokimiyatClassifiers()}.</p>
     */
    @Cacheable(value = "hokimiyatClassifiersInfo", key = "'all'", unless = "#result == null")
    public Map<String, Object> getHokimiyatClassifiersInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : HOKIMIYAT_CLASSIFIER_MAP.entrySet()) {
            String apiKey = entry.getKey();
            String tableName = entry.getValue();

            try {
                if ("h_university".equals(apiKey)) {
                    classifiersList.add(getUniversityClassifierInfoCompat());
                    continue;
                }

                Map<String, Object> classifierInfo = getClassifierInfoForGroup(apiKey, tableName);
                if (classifierInfo != null) {
                    classifiersList.add(classifierInfo);
                }
            } catch (Exception e) {
                log.debug("Error loading hokimiyatInfo classifier {}: {}", apiKey, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Get classifier with items for hokimiyat endpoint
     * Uses custom API key instead of table name
     */
    Map<String, Object> getClassifierWithItemsForHokimiyat(String apiKey, String tableName) {
        if (!tableExists(tableName)) {
            log.debug("Hokimiyat classifier table doesn't exist: {} -> {}", apiKey, tableName);
            return null;
        }

        boolean hasVersion = columnExists(tableName, "version");
        boolean hasActive = columnExists(tableName, "active");
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");

        String countSql = buildCountSql(tableName, hasVersion, hasDeleteTs);
        Map<String, Object> countResult;
        try {
            countResult = jdbcTemplate.queryForMap(countSql);
        } catch (Exception e) {
            log.debug("Error counting {}: {}", tableName, e.getMessage());
            return null;
        }

        long count = ((Number) countResult.get("cnt")).longValue();
        long version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).longValue() : 0;

        String itemsSql = buildItemsSql(tableName, hasActive, hasDeleteTs);
        List<Map<String, Object>> items;
        try {
            items = jdbcTemplate.queryForList(itemsSql);
        } catch (Exception e) {
            log.debug("Error loading items from {}: {}", tableName, e.getMessage());
            items = Collections.emptyList();
        }

        // Add _entityName and build parent nested objects (CUBA compatibility)
        String entityName = getCubaEntityName(tableName);
        for (Map<String, Object> item : items) {
            if (entityName != null) {
                item.put("_entityName", entityName);
            }
            // CUBA: id field must always exist — use code if no UUID id column
            if (!item.containsKey("id") && item.containsKey("code")) {
                item.put("id", item.get("code"));
            }
            // Build nested parent object from flat parent fields (self-join result)
            buildParentNestedObject(item, entityName);
            // Remove null-valued keys — Jackson NON_NULL will skip them anyway,
            // but explicit removal prevents them from appearing if serialization changes
            item.values().removeIf(v -> v == null);
        }

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", getClassifierTitle(apiKey));
        classifierData.put("version", version);
        classifierData.put("count", count);
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(apiKey, classifierData);
        return wrapper;
    }

    /**
     * Get university classifier for hokimiyat endpoint
     */
    Map<String, Object> getUniversityClassifierForHokimiyat() {
        // Version: SUM (old-hemis compatible)
        String statsSql = "SELECT COUNT(*) as cnt, COALESCE(SUM(COALESCE(version, 1)), 0) as ver " +
                           "FROM hemishe_e_university WHERE delete_ts IS NULL";
        Map<String, Object> stats = jdbcTemplate.queryForMap(statsSql);
        long count = ((Number) stats.get("cnt")).longValue();
        long versionSum = ((Number) stats.get("ver")).longValue();

        List<Map<String, Object>> items = buildUniversityItems();

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", "hemishe_h_Oliy ta'lim muassasalari ro'yxati");
        classifierData.put("version", versionSum);
        classifierData.put("count", count);
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("h_university", classifierData);
        return wrapper;
    }

    /**
     * Get classifier info (metadata only) for a specific group entry
     */
    Map<String, Object> getClassifierInfoForGroup(String apiKey, String tableName) {
        if (!tableExists(tableName)) {
            log.debug("Classifier table doesn't exist: {} -> {}", apiKey, tableName);
            return null;
        }

        boolean hasVersion = columnExists(tableName, "version");
        boolean hasDeleteTs = columnExists(tableName, "delete_ts");

        String countSql = buildCountSql(tableName, hasVersion, hasDeleteTs);
        Map<String, Object> countResult;
        try {
            countResult = jdbcTemplate.queryForMap(countSql);
        } catch (Exception e) {
            log.debug("Error counting {}: {}", tableName, e.getMessage());
            return null;
        }

        long count = ((Number) countResult.get("cnt")).longValue();
        long version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).longValue() : 0;

        Map<String, Object> classifierInfo = new LinkedHashMap<>();
        classifierInfo.put("title", getClassifierTitle(apiKey));
        classifierInfo.put("version", version);
        classifierInfo.put("count", count);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(apiKey, classifierInfo);
        return wrapper;
    }

    /**
     * University classifier info — reusable for hokimiyatInfo/stipendInfo endpoints.
     */
    Map<String, Object> getUniversityClassifierInfoCompat() {
        String statsSql = "SELECT COUNT(*) as cnt, COALESCE(SUM(COALESCE(version, 1)), 0) as ver " +
                           "FROM hemishe_e_university WHERE delete_ts IS NULL";
        Map<String, Object> stats = jdbcTemplate.queryForMap(statsSql);
        long count = ((Number) stats.get("cnt")).longValue();
        long versionSum = ((Number) stats.get("ver")).longValue();

        Map<String, Object> classifierInfo = new LinkedHashMap<>();
        classifierInfo.put("title", "hemishe_h_Oliy ta'lim muassasalari ro'yxati");
        classifierInfo.put("version", versionSum);
        classifierInfo.put("count", count);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("h_university", classifierInfo);
        return wrapper;
    }

    /**
     * Convert flat parentId/parentCode/parentName fields into nested parent object.
     * OLD-HEMIS returns parent as: { "_entityName", "_instanceName", "id", "code", "name", "nameEn", "nameRu", "version", "isChecked" }
     */
    private void buildParentNestedObject(Map<String, Object> item, String entityName) {
        Object parentId = item.remove("parentId");
        Object parentCode = item.remove("parentCode");
        Object parentName = item.remove("parentName");
        Object parentNameEn = item.remove("parentNameEn");
        Object parentNameRu = item.remove("parentNameRu");
        Object parentVersion = item.remove("parentVersion");
        Object parentIsChecked = item.remove("parentIsChecked");
        Object parentActive = item.remove("parentActive");

        if (parentId == null) {
            return; // no parent — don't add null parent field
        }

        Map<String, Object> parent = new LinkedHashMap<>();
        if (entityName != null) {
            parent.put("_entityName", entityName);
            parent.put("_instanceName", parentCode != null ? parentCode.toString() + " " + parentName : parentName);
        }
        parent.put("id", parentId.toString());
        parent.put("code", parentCode);
        parent.put("name", parentName);
        if (parentNameEn != null) parent.put("nameEn", parentNameEn);
        if (parentNameRu != null) parent.put("nameRu", parentNameRu);
        parent.put("version", parentVersion);
        if (parentIsChecked != null) parent.put("isChecked", parentIsChecked);
        if (parentActive != null) parent.put("active", parentActive);

        item.put("parent", parent);
    }

    boolean tableExists(String tableName) {
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    private String buildCountSql(String tableName, boolean hasVersion, boolean hasDeleteTs) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as cnt");
        if (hasVersion) {
            // OLD-HEMIS uses SUM(version), NOT MAX(version)
            sql.append(", COALESCE(SUM(COALESCE(version, 1)), 0) as ver");
        } else {
            sql.append(", 1 as ver");
        }
        sql.append(" FROM ").append(tableName);
        if (hasDeleteTs) {
            sql.append(" WHERE delete_ts IS NULL");
        }
        return sql.toString();
    }

    private String buildItemsSql(String tableName, boolean hasActive, boolean hasDeleteTs) {
        boolean hasParentUuid = columnExists(tableName, "_parent");
        boolean hasParentCode = columnExists(tableName, "parent_code");
        // Eski: `active`, Yangi: `is_active` — univer doim `active` kutadi → alias
        boolean hasActiveCol = columnExists(tableName, "active");
        boolean hasIsActiveCol = columnExists(tableName, "is_active");

        StringBuilder sql = new StringBuilder("SELECT ");
        String alias = hasParentUuid ? "t." : "";
        // UUID id (speciality tables have UUID primary key)
        if (columnExists(tableName, "id")) {
            sql.append(alias).append("id, ");
        }
        sql.append(alias).append("code, ").append(alias).append("name");
        // Additional columns for CUBA compatibility
        if (columnExists(tableName, "name_en")) sql.append(", ").append(alias).append("name_en as \"nameEn\"");
        if (columnExists(tableName, "name_ru")) sql.append(", ").append(alias).append("name_ru as \"nameRu\"");
        if (hasActiveCol) sql.append(", ").append(alias).append("active");
        else if (hasIsActiveCol) sql.append(", ").append(alias).append("is_active as active");
        if (columnExists(tableName, "is_checked")) sql.append(", ").append(alias).append("is_checked as \"isChecked\"");
        if (hasParentCode) sql.append(", ").append(alias).append("parent_code as \"parentCode\"");
        sql.append(", COALESCE(").append(alias).append("version, 1) as version");
        // Self-join for _parent UUID: fetch parent entity fields
        if (hasParentUuid) {
            sql.append(", p.id as \"parentId\", p.code as \"parentCode\"");
            sql.append(", p.name as \"parentName\"");
            if (columnExists(tableName, "name_en")) sql.append(", p.name_en as \"parentNameEn\"");
            if (columnExists(tableName, "name_ru")) sql.append(", p.name_ru as \"parentNameRu\"");
            sql.append(", COALESCE(p.version, 1) as \"parentVersion\"");
            if (columnExists(tableName, "is_checked")) sql.append(", p.is_checked as \"parentIsChecked\"");
            if (hasActiveCol) sql.append(", p.active as \"parentActive\"");
            else if (hasIsActiveCol) sql.append(", p.is_active as \"parentActive\"");
        }
        sql.append(" FROM ").append(tableName);
        if (hasParentUuid) {
            sql.append(" t LEFT JOIN ").append(tableName).append(" p ON t._parent = p.id");
        }
        if (hasDeleteTs) {
            sql.append(hasParentUuid ? " WHERE t.delete_ts IS NULL" : " WHERE delete_ts IS NULL");
        }
        sql.append(" ORDER BY ").append(hasParentUuid ? "t." : "").append("code");
        return sql.toString();
    }

    private String getCubaEntityName(String tableName) {
        return switch (tableName) {
            case "hemishe_h_soato" -> "hemishe_HSoato";
            case "hemishe_h_education_type" -> "hemishe_HEducationType";
            case "hemishe_h_education_form" -> "hemishe_HEducationForm";
            case "hemishe_h_education_year" -> "hemishe_HEducationYear";
            case "hemishe_h_student_achievement_type" -> "hemishe_HStudentAchievementType";
            case "hemishe_h_ownership" -> "hemishe_HOwnership";
            case "hemishe_h_course" -> "hemishe_HCourse";
            case "hemishe_h_nationality" -> "hemishe_HNationality";
            case "hemishe_h_speciality_bachelor" -> "hemishe_HSpecialityBachelor";
            case "hemishe_h_speciality_master" -> "hemishe_HSpecialityMaster";
            case "hemishe_h_speciality_doctoral" -> "hemishe_HSpecialityDoctoral";
            case "hemishe_h_citizenship" -> "hemishe_HCitizenship";
            case "hemishe_h_student_social_type" -> "hemishe_HStudentSocialType";
            case "hemishe_h_gender" -> "hemishe_HGender";
            case "hemishe_h_student_type" -> "hemishe_HStudentType";
            case "hemishe_h_payment_form" -> "hemishe_HPaymentForm";
            case "hemishe_h_stipend_rate" -> "hemishe_HStipendRate";
            case "hemishe_h_education_language" -> "hemishe_HEducationLanguage";
            case "hemishe_h_accomodation" -> "hemishe_HAccomodation";
            case "hemishe_h_student_living_status" -> "hemishe_HStudentLivingStatus";
            case "hemishe_h_student_room_mate_type" -> "hemishe_HStudentRoomMateType";
            case "hemishe_h_student_status_type" -> "hemishe_HStudentStatusType";
            case "hemishe_h_country" -> "hemishe_HCountry";
            default -> {
                // Generic conversion: hemishe_h_academic_degree -> hemishe_HAcademicDegree
                if (tableName.startsWith("hemishe_h_")) {
                    String suffix = tableName.substring("hemishe_h_".length());
                    StringBuilder sb = new StringBuilder("hemishe_H");
                    boolean capitalizeNext = true;
                    for (char c : suffix.toCharArray()) {
                        if (c == '_') { capitalizeNext = true; continue; }
                        sb.append(capitalizeNext ? Character.toUpperCase(c) : c);
                        capitalizeNext = false;
                    }
                    yield sb.toString();
                }
                yield null;
            }
        };
    }

    /**
     * Build university items list using JDBC — matching old-hemis hUniversity-view.
     * Includes all local fields + nested objects (soato, universityType,
     * universityContractCategory, versionType).
     */
    List<Map<String, Object>> buildUniversityItems() {
        String sql = "SELECT u.code, u.name, u.active, u.tin, u.address, u.cadastre, " +
                "u.university_url, u.student_url, u.teacher_url, u.uzbmb_url, " +
                "u.gpa_edit, u.accreditation_edit, u.add_student, " +
                "u.add_transfer_student, u.add_foreign_student, " +
                "u.add_academic_mobile_student, u.allow_grouping, " +
                "u.allow_transfer_outside, u.one_id, u.grading_system, " +
                "u.mail_address, u.bank_info, u.accreditation_info, " +
                "u._parent_university, u._terrain, " +
                "COALESCE(u.version, 1) as version, " +
                "u.create_ts, u.created_by, u.update_ts, u.updated_by, " +
                "u._soato, u._soato_region, u._ownership, " +
                "u._university_type, u._university_version, " +
                "u._university_activity_status, u._university_belongs_to, " +
                "u._university_contract_category, " +
                // Nested: soato
                "s.code as soato_code, s.name as soato_name, " +
                // Nested: universityType
                "ut.code as utype_code, ut.name as utype_name, " +
                // Nested: universityContractCategory
                "ucc.code as ucc_code, ucc.name as ucc_name, " +
                // Nested: versionType
                "vt.code as vtype_code, vt.name as vtype_name, " +
                // Nested: ownership
                "ow.code as ow_code, ow.name as ow_name " +
                "FROM hemishe_e_university u " +
                // Yangi jadvallarga yo'naltirilgan — Bosqich 4 refactor
                "LEFT JOIN soato s ON u._soato = s.code " +
                "LEFT JOIN university_type ut ON u._university_type = ut.code " +
                "LEFT JOIN contract_category ucc ON u._university_contract_category = ucc.code " +
                "LEFT JOIN hemis_version vt ON u._university_version = vt.code " +
                "LEFT JOIN ownership ow ON u._ownership = ow.code " +
                "WHERE u.delete_ts IS NULL " +
                "ORDER BY u.code";

        List<Map<String, Object>> rows;
        try {
            rows = jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.warn("University JDBC query failed (JOINs), falling back to simple query: {}", e.getMessage());
            // Fallback: without JOINs (if reference tables don't exist)
            rows = jdbcTemplate.queryForList(
                    "SELECT *, COALESCE(version, 1) as version FROM hemishe_e_university WHERE delete_ts IS NULL ORDER BY code");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("_entityName", "hemishe_EUniversity");
            item.put("_instanceName", row.get("code") + "-" + row.get("name"));
            item.put("id", row.get("code"));

            // All local/scalar fields (camelCase)
            item.put("code", row.get("code"));
            item.put("name", row.get("name"));
            item.put("active", row.get("active"));
            item.put("tin", row.get("tin"));
            item.put("address", row.get("address"));
            item.put("cadastre", row.get("cadastre"));
            item.put("universityUrl", row.get("university_url"));
            item.put("studentUrl", row.get("student_url"));
            item.put("teacherUrl", row.get("teacher_url"));
            item.put("uzbmbUrl", row.get("uzbmb_url"));
            item.put("gpaEdit", row.get("gpa_edit"));
            item.put("accreditationEdit", row.get("accreditation_edit"));
            item.put("addStudent", row.get("add_student"));
            item.put("addTransferStudent", row.get("add_transfer_student"));
            item.put("addForeignStudent", row.get("add_foreign_student"));
            item.put("addAcademicMobileStudent", row.get("add_academic_mobile_student"));
            item.put("allowGrouping", row.get("allow_grouping"));
            item.put("allowTransferOutside", row.get("allow_transfer_outside"));
            item.put("oneId", row.get("one_id"));
            item.put("gradingSystem", row.get("grading_system"));
            item.put("mailAddress", row.get("mail_address"));
            item.put("bankInfo", row.get("bank_info"));
            item.put("accreditationInfo", row.get("accreditation_info"));
            item.put("version", row.get("version"));

            // Audit fields
            Object createTs = row.get("create_ts");
            if (createTs instanceof java.sql.Timestamp ts) {
                item.put("createTs", ts.toLocalDateTime().format(CUBA_DT));
            }
            item.put("createdBy", row.get("created_by"));
            Object updateTs = row.get("update_ts");
            if (updateTs instanceof java.sql.Timestamp ts) {
                item.put("updateTs", ts.toLocalDateTime().format(CUBA_DT));
            }
            item.put("updatedBy", row.get("updated_by"));

            // Nested: soato (old-hemis hUniversity-view property)
            if (row.get("soato_code") != null) {
                Map<String, Object> soato = new LinkedHashMap<>();
                soato.put("_entityName", "hemishe_HSoato");
                soato.put("_instanceName", row.get("soato_code") + "-" + row.get("soato_name"));
                soato.put("id", row.get("soato_code"));
                soato.put("code", row.get("soato_code"));
                soato.put("name", row.get("soato_name"));
                item.put("soato", soato);
            }

            // Nested: universityType
            if (row.get("utype_code") != null) {
                Map<String, Object> uType = new LinkedHashMap<>();
                uType.put("_entityName", "hemishe_HUniversityType");
                uType.put("_instanceName", row.get("utype_code") + "-" + row.get("utype_name"));
                uType.put("id", row.get("utype_code"));
                uType.put("code", row.get("utype_code"));
                uType.put("name", row.get("utype_name"));
                item.put("universityType", uType);
            }

            // Nested: universityContractCategory
            if (row.get("ucc_code") != null) {
                Map<String, Object> ucc = new LinkedHashMap<>();
                ucc.put("_entityName", "hemishe_HUniversityContractCategory");
                ucc.put("_instanceName", row.get("ucc_code") + "-" + row.get("ucc_name"));
                ucc.put("id", row.get("ucc_code"));
                ucc.put("code", row.get("ucc_code"));
                ucc.put("name", row.get("ucc_name"));
                item.put("universityContractCategory", ucc);
            }

            // Nested: versionType
            if (row.get("vtype_code") != null) {
                Map<String, Object> vType = new LinkedHashMap<>();
                vType.put("_entityName", "hemishe_HHemisVersionType");
                vType.put("_instanceName", row.get("vtype_code") + "-" + row.get("vtype_name"));
                vType.put("id", row.get("vtype_code"));
                vType.put("code", row.get("vtype_code"));
                vType.put("name", row.get("vtype_name"));
                item.put("versionType", vType);
            }

            // Nested: ownership (mulkchilik shakli)
            if (row.get("ow_code") != null) {
                Map<String, Object> ow = new LinkedHashMap<>();
                ow.put("_entityName", "hemishe_HOwnership");
                ow.put("_instanceName", row.get("ow_code") + "-" + row.get("ow_name"));
                ow.put("id", row.get("ow_code"));
                ow.put("code", row.get("ow_code"));
                ow.put("name", row.get("ow_name"));
                item.put("ownership", ow);
            }

            // Remove null values (CUBA style)
            item.values().removeIf(v -> v == null);
            items.add(item);
        }

        return items;
    }

    private String getClassifierTitle(String classifierName) {
        return switch (classifierName) {
            case "h_admission_type" -> "Qabul turlari";
            case "h_transfer_type" -> "O'tkazish turlari";
            case "h_external_service_type" -> "Tashqi xizmat turlari";
            case "h_grant_type" -> "Grant turlari";
            case "h_poverty_level" -> "Kam ta'minlanganlik darajasi";
            case "h_certificate_grades" -> "Attestat baholari";
            case "h_certificate_subjects" -> "Attestat fanlari";
            case "h_certificate_names" -> "Attestat nomlari";
            case "h_certificate_type" -> "Attestat turlari";
            case "h_hemis_version_type" -> "HEMIS versiya turlari";
            case "h_outside_activity", "h_outside_activities" -> "Auditoriyadan tashqari mashg'ulotlar";
            case "h_scholarship_decree_type" -> "Stipendiya qaror turlari";
            case "h_resource_type" -> "Resurs turlari";
            case "h_internship_type" -> "Amaliyot turlari";
            case "h_internship_form" -> "Amaliyot shakllari";
            case "h_conduction_form" -> "Dars o'tkazish shakllari";
            case "h_terrain" -> "Joylashgan hudud turi";
            case "h_stipend_rate_category" -> "Stipendiya stavka toifalari";
            case "h_speciality_ordinatura" -> "Ordinatura mutaxassisliklari";
            case "h_academic_mobile_type" -> "Akademik mobillik turlari";
            case "h_contract_class" -> "Shartnoma sinflari";
            case "h_student_living_status" -> "Talaba yashash joyi statusi";
            case "h_student_roommate_type" -> "Birgalikda yashaydiganlar toifasi";
            case "h_student_type" -> "Talaba toifalari";
            case "h_sport_type" -> "Sport turlari";
            case "h_graduate_inactive_type" -> "Bitiruvchi nofaol turlari";
            case "h_graduate_fields_type" -> "Bitiruvchi soha turlari";
            case "h_diplom_blank_status" -> "Diplom blank holati";
            case "h_diplom_blank_category" -> "Diplom blank toifasi";
            case "h_contract_summa_type" -> "Shartnoma summa turlari";
            case "h_contract_type" -> "Shartnoma turlari";
            case "h_decree_type" -> "Buyruq turlari";
            case "h_decree_type_param" -> "Buyruq turi parametrlari";
            case "h_scientific_platform" -> "Ilmiy platformalar";
            case "h_doctorate_student_status" -> "Doktorant holatlari";
            case "h_education_year" -> "O'quv yillar ro'yxati";
            case "h_semester" -> "Semestrlar";
            case "h_science_branch" -> "Fan tarmoqlari";
            case "h_exam_finish" -> "Imtihon yakunlash turlari";
            case "h_final_exam_type" -> "Yakuniy imtihon turlari";
            case "h_locality_type" -> "Joylashuv turlari";
            case "h_academic_reason" -> "Akademik ta'til sabablari";
            case "h_attendance_setting" -> "Davomat sozlamalari";
            case "h_university" -> "Oliy ta'lim muassasalari ro'yxati";
            case "h_country" -> "Davlatlar nomlari";
            case "h_soato" -> "Viloyat va tumanlar";
            case "h_nationality" -> "Millatlar nomlari";
            case "h_citizenship_type" -> "Fuqarolik holatlari turlari";
            case "h_gender" -> "Jins turlari";
            case "h_bachelor_speciality" -> "Bakalavriat ta'lim yo'nalishlari";
            case "h_master_speciality" -> "Magistratura mutaxassisliklari";
            case "h_university_form" -> "OTM shakllari";
            case "h_ownership" -> "OTM mulkchilik shakllari";
            case "h_structure_type" -> "Bo'lim turlari";
            case "h_employee_type" -> "Xodim turlari";
            case "h_teacher_status" -> "O'qituvchi holatlari";
            case "h_employment_staff" -> "Xodim shtat turlari";
            case "h_employment_form" -> "Bandlik shakllari";
            case "h_teacher_position_type" -> "O'qituvchi lavozimlari";
            case "h_qualification" -> "Malaka toifalari";
            case "h_teacher_success" -> "O'qituvchi yutuqlari turlari";
            case "h_academic_degree" -> "Ilmiy darajalar turlari";
            case "h_academic_rank" -> "Ilmiy unvonlar";
            case "h_student_status" -> "Talaba statusi turlari";
            case "h_student_success" -> "Talaba yutuqlari turlari";
            case "h_expel_reason" -> "Chetlatish sabablari";
            case "h_accommodation" -> "Talabalar yashash joylari turlari";
            case "h_doctoral_student_type" -> "Doktorant turlari";
            case "h_social_category" -> "Talabalarning ijtimoiy toifalari";
            case "h_education_type" -> "Ta'lim turlari";
            case "h_education_form" -> "Ta'lim shakllari";
            case "h_language" -> "Ta'lim tillari";
            case "h_marking_system" -> "Baholash tizimlari";
            case "h_grade_type" -> "Baho turlari";
            case "h_exam_type" -> "Imtihon turlari";
            case "h_course" -> "O'quv kurslari";
            case "h_semestr_type" -> "Semestr turlari";
            case "h_education_week_type" -> "Ta'lim hafta turlari";
            case "h_subject_block" -> "Fan bloklari";
            case "h_subject_type" -> "Fan turlari";
            case "h_training_type" -> "Mashg'ulot turlari";
            case "h_project_type" -> "Loyiha turlari";
            case "h_locality" -> "Joylashuvlar";
            case "h_project_currency" -> "Valyutalar";
            case "h_project_executor_type" -> "Loyiha ijrochi turlari";
            case "h_scientific_publication_type" -> "Ilmiy nashr turlari";
            case "h_methodical_publication_type" -> "Metodik nashr turlari";
            case "h_patient_type" -> "Bemor turlari";
            case "h_publication_database" -> "Nashr bazalari";
            case "h_payment_form" -> "To'lov turlari";
            case "h_stipend_rate" -> "Stipendiya turlari";
            case "h_auditorium_type" -> "Auditoriya turlari";
            case "h_device_type" -> "Qurilma turlari";
            default -> classifierName.replace("h_", "").replace("_", " ");
        };
    }
}
