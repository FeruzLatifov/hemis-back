package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.entity.university.UniversityDepartment;
import uz.hemis.domain.repository.UniversityDepartmentRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OLD-HEMIS CUBA nested-classifier map loader.
 *
 * <p><strong>Maqsad:</strong> CUBA REST javoblarida classifier fields ({@code educationType},
 * {@code educationYear}, {@code educationForm}, {@code course}, {@code semester}, {@code faculty},
 * {@code university}) to'liq nested obyekt sifatida qaytariladi. Har lookup ilgari alohida
 * JdbcTemplate yoki JPA query qilardi → bitta response uchun 7+ DB roundtrip (mapper N+1).</p>
 *
 * <p><strong>Yechim:</strong> Bu yerda har {@code build*Map(code)} metod {@code @Cacheable}
 * — alohida bean'da Spring AOP proxy ishlaydi (StudentLoader pattern: same-class call
 * cache bypass'ini oldini olish).</p>
 *
 * <p><strong>Cache:</strong> {@code legacyClassifierMaps} (24h TTL, Redis L2). Reference data
 * (education_*, course, semester) kamdan-kam o'zgaradi. Universitet/department qisqaroq TTL
 * yoki mutation evict bilan boshqarish kerak bo'lsa, alohida cache namespace ajratiladi.</p>
 *
 * @since 2.5.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LegacyClassifierMapLoader {

    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final JdbcTemplate jdbcTemplate;
    private final UniversityRepository universityRepository;
    private final UniversityDepartmentRepository universityDepartmentRepository;

    @Cacheable(value = "legacyClassifierMaps", key = "'educationType:' + #code", unless = "#result == null")
    public Map<String, Object> educationTypeMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationType");
        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                    "SELECT code, name, name_ru, name_en, is_active as active, version, created_by, updated_by, "
                            + "created_at as create_ts, updated_at as update_ts "
                            + "FROM education_type WHERE code = ?", code);
            putWithFallback(result, code, data, true, true);
        } catch (Exception e) {
            log.warn("Education type not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }
        return result;
    }

    @Cacheable(value = "legacyClassifierMaps", key = "'educationYear:' + #code", unless = "#result == null")
    public Map<String, Object> educationYearMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationYear");
        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                    "SELECT code, name, is_active as active, version, created_by, "
                            + "created_at as create_ts, updated_at as update_ts "
                            + "FROM education_year WHERE code = ?", code);
            result.put("id", code);
            result.put("code", code);
            putIfPresent(result, "createdBy", data.get("created_by"));
            putIfPresent(result, "name", data.get("name"));
            putIfPresent(result, "active", data.get("active"));
            putTimestampIfPresent(result, "createTs", data.get("create_ts"));
            putTimestampIfPresent(result, "updateTs", data.get("update_ts"));
            putIfPresent(result, "version", data.get("version"));
        } catch (Exception e) {
            log.warn("Education year not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }
        return result;
    }

    @Cacheable(value = "legacyClassifierMaps", key = "'educationForm:' + #code", unless = "#result == null")
    public Map<String, Object> educationFormMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HEducationForm");
        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                    "SELECT code, name, name_ru, name_en, is_active as active, version, created_by, updated_by, "
                            + "created_at as create_ts, updated_at as update_ts "
                            + "FROM education_form WHERE code = ?", code);
            putWithFallback(result, code, data, true, true);
        } catch (Exception e) {
            log.warn("Education form not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }
        return result;
    }

    @Cacheable(value = "legacyClassifierMaps", key = "'course:' + #code", unless = "#result == null")
    public Map<String, Object> courseMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HCourse");
        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                    "SELECT code, name, is_active as active, version, created_by, created_at as create_ts "
                            + "FROM course WHERE code = ?", code);
            result.put("id", code);
            result.put("code", code);
            putIfPresent(result, "createdBy", data.get("created_by"));
            putIfPresent(result, "name", data.get("name"));
            putIfPresent(result, "active", data.get("active"));
            putTimestampIfPresent(result, "createTs", data.get("create_ts"));
            putIfPresent(result, "version", data.get("version"));
        } catch (Exception e) {
            log.warn("Course not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }
        return result;
    }

    @Cacheable(value = "legacyClassifierMaps", key = "'semester:' + #code", unless = "#result == null")
    public Map<String, Object> semesterMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_HSemester");
        try {
            Map<String, Object> data = jdbcTemplate.queryForMap(
                    "SELECT code, name, is_active as active, version, created_by, updated_by, "
                            + "created_at as create_ts FROM semester WHERE code = ?", code);
            result.put("id", code);
            result.put("code", code);
            putIfPresent(result, "updatedBy", data.get("updated_by"));
            putIfPresent(result, "createdBy", data.get("created_by"));
            putIfPresent(result, "name", data.get("name"));
            putIfPresent(result, "active", data.get("active"));
            putTimestampIfPresent(result, "createTs", data.get("create_ts"));
            putIfPresent(result, "version", data.get("version"));
        } catch (Exception e) {
            log.warn("Semester not found: {}", code);
            result.put("id", code);
            result.put("code", code);
        }
        return result;
    }

    @Cacheable(value = "legacyClassifierMaps", key = "'university:' + #code", unless = "#result == null")
    public Map<String, Object> universityMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_EUniversity");
        Optional<University> uni = universityRepository.findById(code);
        if (uni.isPresent()) {
            University u = uni.get();
            result.put("id", code);
            putIfPresent(result, "studentUrl", u.getStudentUrl());
            putIfPresent(result, "gradingSystem", u.getGradingSystem());
            result.put("code", code);
            putIfPresent(result, "uzbmbUrl", u.getUzbmbUrl());
            putIfPresent(result, "tin", u.getTin());
            if (u.getCreateTs() != null) result.put("createTs", u.getCreateTs().format(DATETIME_FORMAT));
            putIfPresent(result, "addStudent", u.getAddStudent());
            putIfPresent(result, "address", u.getAddress());
            putIfPresent(result, "updatedBy", u.getUpdatedBy());
            putIfPresent(result, "accreditationEdit", u.getAccreditationEdit());
            putIfPresent(result, "active", u.getActive());
            putIfPresent(result, "version", u.getVersion());
            putIfPresent(result, "oneId", u.getOneId());
            putIfPresent(result, "allowGrouping", u.getAllowGrouping());
            putIfPresent(result, "allowTransferOutside", u.getAllowTransferOutside());
            putIfPresent(result, "createdBy", u.getCreatedBy());
            putIfPresent(result, "name", u.getName());
            putIfPresent(result, "gpaEdit", u.getGpaEdit());
            if (u.getUpdateTs() != null) result.put("updateTs", u.getUpdateTs().format(DATETIME_FORMAT));
        } else {
            result.put("id", code);
            result.put("code", code);
        }
        return result;
    }

    @Cacheable(value = "legacyClassifierMaps", key = "'faculty:' + #code", unless = "#result == null")
    public Map<String, Object> facultyMap(String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("_entityName", "hemishe_EUniversityDepartment");
        Optional<UniversityDepartment> dept = universityDepartmentRepository.findByCode(code);
        if (dept.isPresent()) {
            UniversityDepartment d = dept.get();
            result.put("id", code);
            result.put("code", code);
            putIfPresent(result, "updatedBy", d.getUpdatedBy());
            putIfPresent(result, "version", d.getVersion());
            putIfPresent(result, "nameUz", d.getNameUz());
            putIfPresent(result, "nameRu", d.getNameRu());
            putIfPresent(result, "createdBy", d.getCreatedBy());
            if (d.getCreateTs() != null) result.put("createTs", d.getCreateTs().format(DATETIME_FORMAT));
            if (d.getUpdateTs() != null) result.put("updateTs", d.getUpdateTs().format(DATETIME_FORMAT));
            putIfPresent(result, "status", d.getStatus());
        } else {
            result.put("id", code);
            result.put("code", code);
        }
        return result;
    }

    // =====================================================
    // Helpers — preserve OLD-HEMIS field-order convention
    // =====================================================

    private void putWithFallback(Map<String, Object> result, String code, Map<String, Object> data,
                                 boolean hasNameRu, boolean hasNameEn) {
        result.put("id", code);
        if (hasNameRu) putIfPresent(result, "nameRu", data.get("name_ru"));
        result.put("code", code);
        putIfPresent(result, "updatedBy", data.get("updated_by"));
        putIfPresent(result, "createdBy", data.get("created_by"));
        putIfPresent(result, "name", data.get("name"));
        putIfPresent(result, "active", data.get("active"));
        putTimestampIfPresent(result, "createTs", data.get("create_ts"));
        if (hasNameEn) putIfPresent(result, "nameEn", data.get("name_en"));
        putTimestampIfPresent(result, "updateTs", data.get("update_ts"));
        putIfPresent(result, "version", data.get("version"));
    }

    private static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }

    private static void putTimestampIfPresent(Map<String, Object> map, String key, Object ts) {
        if (ts == null) return;
        if (ts instanceof java.sql.Timestamp t) {
            map.put(key, t.toLocalDateTime().format(DATETIME_FORMAT));
        } else if (ts instanceof LocalDateTime ldt) {
            map.put(key, ldt.format(DATETIME_FORMAT));
        } else {
            map.put(key, ts.toString());
        }
    }
}
