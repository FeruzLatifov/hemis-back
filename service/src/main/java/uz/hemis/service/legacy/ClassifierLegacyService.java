package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classifier Legacy Service - CUBA compatible operations for classifier entities
 *
 * Handles read-only operations for reference/classifier tables:
 * - hemishe_HEducationType
 * - hemishe_HEducationForm
 * - hemishe_HCourse
 * - hemishe_HEducationYear
 * - hemishe_HTransferType
 * - hemishe_HAdmissionType
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClassifierLegacyService {

    private final EducationTypeRepository educationTypeRepository;
    private final EducationFormRepository educationFormRepository;
    private final HCourseRepository hCourseRepository;
    private final EducationYearRepository educationYearRepository;
    private final TransferTypeRepository transferTypeRepository;
    private final AdmissionTypeRepository admissionTypeRepository;
    private final HUniversityDepartmentTypeRepository hUniversityDepartmentTypeRepository;
    private final uz.hemis.domain.repository.UniversityRepository universityRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter CUBA_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * OLD-HEMIS stipend classifiers - exact mapping (13 ta)
     * Key: API response dagi nom
     * Value: Database table nomi
     */
    private static final Map<String, String> STIPEND_CLASSIFIER_MAP = new LinkedHashMap<>() {{
        put("h_soato", "hemishe_h_soato");
        put("h_education_type", "hemishe_h_education_type");
        put("h_education_form", "hemishe_h_education_form");
        put("h_education_year", "hemishe_h_education_year");
        put("h_student_success", "hemishe_h_student_achievement_type");
        put("h_university", "hemishe_e_university"); // Special: EUniversity table
        put("h_ownership", "hemishe_h_ownership");
        put("h_course", "hemishe_h_course");
        put("h_nationality", "hemishe_h_nationality");
        put("h_bachelor_speciality", "hemishe_h_speciality_bachelor");
        put("h_master_speciality", "hemishe_h_speciality_master");
        put("h_citizenship_type", "hemishe_h_citizenship");
        put("h_social_category", "hemishe_h_student_social_type");
    }};

    /**
     * OLD-HEMIS hokimiyat classifiers - exact mapping (20 ta)
     * Key: API response dagi nom
     * Value: Database table nomi
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

    // ==================== EducationType ====================

    public List<EducationType> findAllEducationTypes() {
        return educationTypeRepository.findAll();
    }

    public Optional<EducationType> findEducationTypeByCode(String code) {
        return educationTypeRepository.findById(code);
    }

    public Map<String, Object> toEducationTypeMap(EducationType e, Boolean returnNulls) {
        return classifierToMap("hemishe_HEducationType", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
    }

    // ==================== EducationForm ====================

    public List<EducationForm> findAllEducationForms() {
        return educationFormRepository.findAll();
    }

    public Optional<EducationForm> findEducationFormByCode(String code) {
        return educationFormRepository.findById(code);
    }

    public Map<String, Object> toEducationFormMap(EducationForm e, Boolean returnNulls) {
        return classifierToMap("hemishe_HEducationForm", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
    }

    // ==================== HCourse ====================

    public List<HCourse> findAllCourses() {
        return hCourseRepository.findAll();
    }

    public Optional<HCourse> findCourseByCode(String code) {
        return hCourseRepository.findById(code);
    }

    public Map<String, Object> toCourseMap(HCourse e, Boolean returnNulls) {
        return classifierToMap("hemishe_HCourse", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
    }

    // ==================== EducationYear ====================

    public List<EducationYear> findAllEducationYears() {
        return educationYearRepository.findAll();
    }

    public Optional<EducationYear> findEducationYearByCode(String code) {
        return educationYearRepository.findById(code);
    }

    public Map<String, Object> toEducationYearMap(EducationYear e, Boolean returnNulls) {
        return classifierToMap("hemishe_HEducationYear", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
    }

    // ==================== TransferType ====================

    public List<TransferType> findAllTransferTypes() {
        return transferTypeRepository.findAll();
    }

    public Optional<TransferType> findTransferTypeByCode(String code) {
        return transferTypeRepository.findById(code);
    }

    public Map<String, Object> toTransferTypeMap(TransferType e, Boolean returnNulls) {
        return classifierToMap("hemishe_HTransferType", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
    }

    // ==================== AdmissionType ====================

    public List<AdmissionType> findAllAdmissionTypes() {
        return admissionTypeRepository.findAll();
    }

    public Optional<AdmissionType> findAdmissionTypeByCode(String code) {
        return admissionTypeRepository.findById(code);
    }

    public Map<String, Object> toAdmissionTypeMap(AdmissionType e, Boolean returnNulls) {
        return classifierToMap("hemishe_HAdmissionType", e.getCode(), e.getName(), e.getNameRu(), e.getNameEn(),
                e.getVersion(), e.getActive(), e.getCreateTs(), e.getCreatedBy(),
                e.getUpdateTs(), e.getUpdatedBy(), e.getDeleteTs(), e.getDeletedBy(), returnNulls);
    }

    // ==================== Shared CUBA Map Builder ====================

    /**
     * Convert classifier entity to CUBA-compatible Map
     */
    public Map<String, Object> classifierToMap(String entityName, String code, String name,
            String nameRu, String nameEn, Integer version, Boolean active,
            LocalDateTime createTs, String createdBy,
            LocalDateTime updateTs, String updatedBy,
            LocalDateTime deleteTs, String deletedBy,
            Boolean returnNulls) {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", entityName);
        map.put("_instanceName", name);
        map.put("code", code);
        map.put("name", name);

        CubaEntityMapHelper.putIfNotNull(map, "nameRu", nameRu, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "nameEn", nameEn, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "version", version, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "active", active, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createTs", createTs != null ? createTs.format(CUBA_DT) : null, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "createdBy", createdBy, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updateTs", updateTs != null ? updateTs.format(CUBA_DT) : null, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "updatedBy", updatedBy, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deleteTs", deleteTs != null ? deleteTs.format(CUBA_DT) : null, returnNulls);
        CubaEntityMapHelper.putIfNotNull(map, "deletedBy", deletedBy, returnNulls);

        return map;
    }

    /**
     * Apply pagination to list
     */
    public <T> List<T> applyPagination(List<T> list, int limit, int offset) {
        int from = Math.min(offset, list.size());
        int to = Math.min(from + limit, list.size());
        return list.subList(from, to);
    }

    // ==================== Dynamic Classifier Methods ====================

    /**
     * Get all classifiers with items (OLD-HEMIS /allItems endpoint)
     */
    public Map<String, Object> getAllClassifiersWithItems() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();
        List<String> tables = getClassifierTables();

        for (String table : tables) {
            try {
                Map<String, Object> classifierData = getClassifierWithItems(table);
                if (classifierData != null) {
                    classifiersList.add(classifierData);
                }
            } catch (Exception e) {
                log.debug("Error loading classifier {}: {}", table, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get all classifiers info (metadata only) (OLD-HEMIS /info endpoint)
     */
    public Map<String, Object> getAllClassifiersInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();
        List<String> tables = getClassifierTables();

        for (String table : tables) {
            try {
                Map<String, Object> classifierInfo = getClassifierInfo(table);
                if (classifierInfo != null) {
                    classifiersList.add(classifierInfo);
                }
            } catch (Exception e) {
                log.debug("Error loading classifier info {}: {}", table, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get single classifier by name (OLD-HEMIS /single endpoint)
     */
    public Map<String, Object> getSingleClassifier(String classifier) {
        if (classifier == null || classifier.isEmpty()) {
            return null;
        }

        // Special handling for h_university
        if ("h_university".equals(classifier)) {
            return getUniversityClassifier();
        }

        String tableName = "hemishe_" + classifier;
        if (!tableExists(tableName)) {
            return null;
        }

        try {
            Map<String, Object> classifierData = getClassifierWithItems(tableName);
            if (classifierData == null) {
                return null;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("classifier", classifierData);
            return result;
        } catch (Exception e) {
            log.error("Error loading classifier {}: {}", classifier, e.getMessage());
            return null;
        }
    }

    /**
     * Get hokimiyat classifiers (OLD-HEMIS /hokimiyat endpoint)
     * OLD-HEMIS bilan 100% mos - 20 ta classifier
     */
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
     * Get classifier with items for hokimiyat endpoint
     * Uses custom API key instead of table name
     */
    private Map<String, Object> getClassifierWithItemsForHokimiyat(String apiKey, String tableName) {
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
        int version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).intValue() : 1;

        String itemsSql = buildItemsSql(tableName, hasActive, hasDeleteTs);
        List<Map<String, Object>> items;
        try {
            items = jdbcTemplate.queryForList(itemsSql);
        } catch (Exception e) {
            log.debug("Error loading items from {}: {}", tableName, e.getMessage());
            items = Collections.emptyList();
        }

        // Add _entityName to each item (CUBA compatibility)
        String entityName = getCubaEntityName(tableName);
        if (entityName != null) {
            for (Map<String, Object> item : items) {
                item.put("_entityName", entityName);
            }
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
            default -> null;
        };
    }

    /**
     * Get university classifier for hokimiyat endpoint
     */
    private Map<String, Object> getUniversityClassifierForHokimiyat() {
        List<uz.hemis.domain.entity.University> universities = universityRepository.findAll();

        List<Map<String, Object>> items = new ArrayList<>();
        int maxVersion = 1;
        for (uz.hemis.domain.entity.University uni : universities) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", uni.getCode());
            item.put("name", uni.getName());
            item.put("active", uni.getActive());
            item.put("version", uni.getVersion());
            items.add(item);
            if (uni.getVersion() != null && uni.getVersion() > maxVersion) {
                maxVersion = uni.getVersion();
            }
        }

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", "Oliy ta'lim muassasalari ro'yxati");
        classifierData.put("version", maxVersion);
        classifierData.put("count", universities.size());
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("h_university", classifierData);
        return wrapper;
    }

    /**
     * Get hokimiyat classifiers info (metadata only, no items)
     * OLD-HEMIS /hokimiyatInfo endpoint
     */
    public Map<String, Object> getHokimiyatClassifiersInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : HOKIMIYAT_CLASSIFIER_MAP.entrySet()) {
            String apiKey = entry.getKey();
            String tableName = entry.getValue();

            try {
                if ("h_university".equals(apiKey)) {
                    List<uz.hemis.domain.entity.University> universities = universityRepository.findAll();
                    int maxVersion = universities.stream()
                            .filter(u -> u.getVersion() != null)
                            .mapToInt(uz.hemis.domain.entity.University::getVersion)
                            .max().orElse(1);

                    Map<String, Object> classifierInfo = new LinkedHashMap<>();
                    classifierInfo.put("title", "Oliy ta'lim muassasalari ro'yxati");
                    classifierInfo.put("version", maxVersion);
                    classifierInfo.put("count", universities.size());

                    Map<String, Object> wrapper = new LinkedHashMap<>();
                    wrapper.put(apiKey, classifierInfo);
                    classifiersList.add(wrapper);
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

    /**
     * Get stipend classifiers with items
     * OLD-HEMIS /stipend endpoint (13 ta classifier)
     */
    public Map<String, Object> getStipendClassifiers() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : STIPEND_CLASSIFIER_MAP.entrySet()) {
            String apiKey = entry.getKey();
            String tableName = entry.getValue();

            try {
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
                log.debug("Error loading stipend classifier {}: {}", apiKey, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get stipend classifiers info (metadata only, no items)
     * OLD-HEMIS /stipendInfo endpoint
     */
    public Map<String, Object> getStipendClassifiersInfo() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        List<Map<String, Object>> classifiersList = new ArrayList<>();

        for (Map.Entry<String, String> entry : STIPEND_CLASSIFIER_MAP.entrySet()) {
            String apiKey = entry.getKey();
            String tableName = entry.getValue();

            try {
                if ("h_university".equals(apiKey)) {
                    List<uz.hemis.domain.entity.University> universities = universityRepository.findAll();
                    int maxVersion = universities.stream()
                            .filter(u -> u.getVersion() != null)
                            .mapToInt(uz.hemis.domain.entity.University::getVersion)
                            .max().orElse(1);

                    Map<String, Object> classifierInfo = new LinkedHashMap<>();
                    classifierInfo.put("title", "Oliy ta'lim muassasalari ro'yxati");
                    classifierInfo.put("version", maxVersion);
                    classifierInfo.put("count", universities.size());

                    Map<String, Object> wrapper = new LinkedHashMap<>();
                    wrapper.put(apiKey, classifierInfo);
                    classifiersList.add(wrapper);
                    continue;
                }

                Map<String, Object> classifierInfo = getClassifierInfoForGroup(apiKey, tableName);
                if (classifierInfo != null) {
                    classifiersList.add(classifierInfo);
                }
            } catch (Exception e) {
                log.debug("Error loading stipendInfo classifier {}: {}", apiKey, e.getMessage());
            }
        }

        result.put("classifiers", classifiersList);
        return result;
    }

    /**
     * Get classifier info (metadata only) for a specific group entry
     */
    private Map<String, Object> getClassifierInfoForGroup(String apiKey, String tableName) {
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
        int version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).intValue() : 1;

        Map<String, Object> classifierInfo = new LinkedHashMap<>();
        classifierInfo.put("title", getClassifierTitle(apiKey));
        classifierInfo.put("version", version);
        classifierInfo.put("count", count);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(apiKey, classifierInfo);
        return wrapper;
    }

    // ==================== Helper Methods ====================

    private List<String> getClassifierTables() {
        try {
            String sql = "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = 'public' AND table_name LIKE 'hemishe_h_%' " +
                    "ORDER BY table_name";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            log.error("Error getting classifier tables: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean tableExists(String tableName) {
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

    private Map<String, Object> getClassifierWithItems(String tableName) {
        String classifierName = tableName.replace("hemishe_", "");
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
        int version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).intValue() : 1;

        String itemsSql = buildItemsSql(tableName, hasActive, hasDeleteTs);
        List<Map<String, Object>> items;
        try {
            items = jdbcTemplate.queryForList(itemsSql);
        } catch (Exception e) {
            log.debug("Error loading items from {}: {}", tableName, e.getMessage());
            items = Collections.emptyList();
        }

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", getClassifierTitle(classifierName));
        classifierData.put("version", version);
        classifierData.put("count", count);
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(classifierName, classifierData);
        return wrapper;
    }

    private Map<String, Object> getClassifierInfo(String tableName) {
        String classifierName = tableName.replace("hemishe_", "");
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
        int version = countResult.get("ver") != null ? ((Number) countResult.get("ver")).intValue() : 1;

        Map<String, Object> classifierInfo = new LinkedHashMap<>();
        classifierInfo.put("title", getClassifierTitle(classifierName));
        classifierInfo.put("version", version);
        classifierInfo.put("count", count);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put(classifierName, classifierInfo);
        return wrapper;
    }

    private Map<String, Object> getUniversityClassifier() {
        List<uz.hemis.domain.entity.University> universities = universityRepository.findAll();

        List<Map<String, Object>> items = new ArrayList<>();
        int maxVersion = 1;
        for (uz.hemis.domain.entity.University uni : universities) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", uni.getCode());
            item.put("name", uni.getName());
            item.put("active", uni.getActive());
            item.put("version", uni.getVersion());
            items.add(item);
            if (uni.getVersion() != null && uni.getVersion() > maxVersion) {
                maxVersion = uni.getVersion();
            }
        }

        Map<String, Object> classifierData = new LinkedHashMap<>();
        classifierData.put("title", "Oliy ta'lim muassasalari");
        classifierData.put("version", maxVersion);
        classifierData.put("count", universities.size());
        classifierData.put("items", items);

        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("h_university", classifierData);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("classifier", wrapper);
        return result;
    }

    private String buildCountSql(String tableName, boolean hasVersion, boolean hasDeleteTs) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as cnt");
        if (hasVersion) {
            sql.append(", COALESCE(MAX(version), 1) as ver");
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
        StringBuilder sql = new StringBuilder("SELECT ");
        // UUID id (speciality tables have UUID primary key)
        if (columnExists(tableName, "id")) {
            sql.append("id, ");
        }
        sql.append("code, name");
        // Additional columns for CUBA compatibility
        if (columnExists(tableName, "name_en")) sql.append(", name_en as \"nameEn\"");
        if (columnExists(tableName, "name_ru")) sql.append(", name_ru as \"nameRu\"");
        if (hasActive) sql.append(", active");
        if (columnExists(tableName, "is_checked")) sql.append(", is_checked as \"isChecked\"");
        if (columnExists(tableName, "parent_code")) sql.append(", parent_code as \"parentCode\"");
        sql.append(", COALESCE(version, 1) as version FROM ").append(tableName);
        if (hasDeleteTs) {
            sql.append(" WHERE delete_ts IS NULL");
        }
        sql.append(" ORDER BY code");
        return sql.toString();
    }

    private String getClassifierTitle(String classifierName) {
        return switch (classifierName) {
            // OLD-HEMIS hokimiyat classifiers (exact titles)
            case "h_education_type" -> "Ta'lim turlari";
            case "h_education_form" -> "Ta'lim shakllari";
            case "h_education_year" -> "O'quv yillar ro'yxati";
            case "h_university" -> "Oliy ta'lim muassasalari ro'yxati";
            case "h_ownership" -> "OTM mulkchilik shakllari";
            case "h_course" -> "O'quv kurslari";
            case "h_nationality" -> "Millatlar nomlari";
            case "h_citizenship_type" -> "Fuqarolik holatlari turlari";
            case "h_social_category" -> "Talabalarning ijtimoiy toifalari";
            case "h_gender" -> "Jins turlari";
            case "h_student_type" -> "Talaba toifalari";
            case "h_payment_form" -> "To'lov turlari";
            case "h_stipend_rate" -> "Stipendiya turlari";
            case "h_language" -> "Ta'lim tillari";
            case "h_accommodation" -> "Talabalar yashash joylari turlari";
            case "h_student_living_status" -> "Talaba yashash joyi statusi";
            case "h_student_roommate_type" -> "Birgalikda yashaydiganlar toifasi";
            case "h_student_status" -> "Talaba statusi turlari";
            case "h_soato" -> "Viloyat va tumanlar";
            case "h_country" -> "Davlatlar nomlari";
            // Other classifiers
            case "h_citizenship" -> "Fuqaroliklar";
            case "h_education_language" -> "Ta'lim tillari";
            case "h_student_status_type" -> "Talaba holatlari";
            case "h_university_department_type" -> "Bo'lim turlari";
            case "h_teacher_position_type" -> "O'qituvchi lavozimlari";
            case "h_academic_degree" -> "Ilmiy darajalar";
            case "h_academic_rank" -> "Ilmiy unvonlar";
            case "h_employment_form" -> "Bandlik shakllari";
            case "h_employment_type" -> "Bandlik turlari";
            case "h_specialty_direction" -> "Mutaxassislik yo'nalishlari";
            case "h_transfer_type" -> "O'tkazish turlari";
            case "h_admission_type" -> "Qabul turlari";
            case "h_student_success" -> "Talaba yutuqlari turlari";
            case "h_bachelor_speciality" -> "Bakalavriat ta'lim yo'nalishlari";
            case "h_master_speciality" -> "Magistratura mutaxassisliklari";
            default -> classifierName.replace("h_", "").replace("_", " ");
        };
    }

    // ==================== HUniversityDepartmentType ====================

    public Optional<HUniversityDepartmentType> findDepartmentTypeById(String code) {
        return hUniversityDepartmentTypeRepository.findById(code);
    }

    public List<HUniversityDepartmentType> findAllDepartmentTypes() {
        return hUniversityDepartmentTypeRepository.findAll();
    }

    @Transactional
    public HUniversityDepartmentType saveDepartmentType(HUniversityDepartmentType entity) {
        return hUniversityDepartmentTypeRepository.save(entity);
    }

    @Transactional
    public void softDeleteDepartmentType(HUniversityDepartmentType entity) {
        entity.setDeleteTs(LocalDateTime.now());
        hUniversityDepartmentTypeRepository.save(entity);
    }

    /**
     * Create or restore (if soft-deleted) a HUniversityDepartmentType.
     * Uses native query to bypass @Where(deleteTs IS NULL) filter.
     *
     * @param code     the code (primary key)
     * @param name     the name
     * @param nameRu   the Russian name
     * @param nameEn   the English name
     * @return the created or restored entity
     */
    @Transactional
    public HUniversityDepartmentType createOrRestoreDepartmentType(String code, String name, String nameRu, String nameEn) {
        // Check if entity exists (including soft-deleted) using native query
        String checkSql = "SELECT COUNT(*) FROM hemishe_h_university_department_type WHERE code = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, code);

        if (count != null && count > 0) {
            // Entity exists - update it (restore if soft-deleted)
            String updateSql = """
                UPDATE hemishe_h_university_department_type
                SET name = ?, name_ru = ?, name_en = ?, active = true,
                    delete_ts = NULL, deleted_by = NULL, update_ts = ?
                WHERE code = ?
                """;
            jdbcTemplate.update(updateSql, name, nameRu, nameEn, LocalDateTime.now(), code);
        } else {
            // Create new entity
            String insertSql = """
                INSERT INTO hemishe_h_university_department_type (code, name, name_ru, name_en, active, create_ts, update_ts, version)
                VALUES (?, ?, ?, ?, true, ?, ?, 1)
                """;
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update(insertSql, code, name, nameRu, nameEn, now, now);
        }

        // Return the entity (now visible since deleteTs is NULL)
        return hUniversityDepartmentTypeRepository.findById(code)
                .orElseThrow(() -> new IllegalStateException("Failed to create/restore department type: " + code));
    }

    /**
     * Convert HUniversityDepartmentType to CUBA-compatible map.
     *
     * <p><strong>OLD-HEMIS FIELD ORDER (100% compatible):</strong></p>
     * <ul>
     *   <li>_entityName, _instanceName, id - har doim</li>
     *   <li>nameRu, deleteTs - returnNulls=true bo'lganda</li>
     *   <li>code, name, active - har doim (null bo'lmasa)</li>
     *   <li>nameEn - returnNulls=true bo'lganda</li>
     *   <li>version - har doim</li>
     *   <li>deletedBy - returnNulls=true bo'lganda</li>
     * </ul>
     */
    public Map<String, Object> toDepartmentTypeMap(HUniversityDepartmentType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS exact field order
        map.put("_entityName", "hemishe_HUniversityDepartmentType");
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());

        // returnNulls=true bo'lganda nameRu
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);

        // returnNulls=true bo'lganda deleteTs
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);

        // Asosiy maydonlar (har doim, null bo'lmasa)
        putIfNotNull(map, "code", entity.getCode(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        // returnNulls=true bo'lganda nameEn
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);

        // Version (har doim)
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        // returnNulls=true bo'lganda deletedBy
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    public void updateDepartmentTypeFromMap(HUniversityDepartmentType entity, Map<String, Object> body) {
        if (body.containsKey("name")) {
            Object val = body.get("name");
            entity.setName(val != null ? val.toString() : null);
        }
        if (body.containsKey("active")) {
            Object val = body.get("active");
            if (val instanceof Boolean) {
                entity.setActive((Boolean) val);
            } else if (val != null) {
                entity.setActive(Boolean.parseBoolean(val.toString()));
            }
        }
        entity.setUpdateTs(LocalDateTime.now());
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
