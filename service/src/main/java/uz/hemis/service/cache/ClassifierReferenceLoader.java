package uz.hemis.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.legacy.StudentLegacyDto;

import java.util.Map;

/**
 * Cached loader for CUBA-format classifier references (hemishe_h_*).
 *
 * <p>Extracted from {@code StudentLegacyMapper} so that {@link Cacheable} works
 * through the Spring AOP proxy (private method calls inside the same bean would
 * skip the cache aspect).</p>
 *
 * <p>Each (tableName, entityName, code) tuple is cached for 24 h. The reference
 * tables are admin-edited only; mutation paths must evict via
 * {@link #evictAll()} or selective {@code @CacheEvict} on the writer.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ClassifierReferenceLoader {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Load a simple classifier reference row and shape it into a CUBA
     * {@link StudentLegacyDto.SimpleReferenceDto}, mirroring the per-entity
     * field projection that OLD-HEMIS clients depend on.
     *
     * <p>Returns {@code null} for blank codes or missing rows so callers can
     * skip the field; null results are not cached.</p>
     */
    @Cacheable(
            value = "classifierReference",
            key = "#tableName + ':' + #entityName + ':' + #code",
            unless = "#result == null"
    )
    public StudentLegacyDto.SimpleReferenceDto loadSimpleReference(String tableName, String entityName, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            String sql = "SELECT code, name, name_ru, name_en, active, version FROM " + tableName
                    + " WHERE code = ? AND delete_ts IS NULL";
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, code);

            StudentLegacyDto.SimpleReferenceDto ref = new StudentLegacyDto.SimpleReferenceDto();
            ref.setEntityName(entityName);
            ref.setId(code);
            String name = (String) row.get("name");
            ref.setName(name);

            // _instanceName format depends on entity type
            if ("hemishe_HEducationYear".equals(entityName)) {
                ref.setInstanceName(name != null ? name : code);
            } else if ("hemishe_HPaymentForm".equals(entityName)) {
                ref.setInstanceName(code + " - " + (name != null ? name : ""));
            } else if ("hemishe_HExpel".equals(entityName)) {
                ref.setInstanceName(name != null ? name : "");
            } else {
                ref.setInstanceName(code + " " + (name != null ? name : ""));
            }

            // Per-entity field projection (verified against actual OLD-HEMIS responses)
            switch (entityName) {
                case "hemishe_HCountry":
                case "hemishe_HEducationLanguage":
                case "hemishe_HEducationForm":
                case "hemishe_HCourse":
                case "hemishe_HPaymentForm":
                case "hemishe_HCitizenship":
                case "hemishe_HNationality":
                case "hemishe_HAccomodation":
                case "hemishe_HStudentLivingStatus":
                case "hemishe_HStudentRoomMateType":
                case "hemishe_HAcademicReason":
                case "hemishe_HAcademicMobileType":
                case "hemishe_HGrantType":
                case "hemishe_HAdmissionType":
                case "hemishe_HTransferType":
                case "hemishe_HDoctoralStudentType":
                case "hemishe_HStipendRate":
                case "hemishe_HStudentType":
                case "hemishe_HPovertyLevel":
                    ref.setCode(code);
                    break;
                case "hemishe_HExpel":
                    ref.setActive(getBoolean(row, "active"));
                    break;
                case "hemishe_HEducationType":
                    ref.setCode(code);
                    ref.setNameRu((String) row.get("name_ru"));
                    ref.setNameEn((String) row.get("name_en"));
                    ref.setActive(getBoolean(row, "active"));
                    break;
                case "hemishe_HStudentSocialType":
                    ref.setCode(code);
                    ref.setActive(getBoolean(row, "active"));
                    break;
                case "hemishe_HGender":
                case "hemishe_HStudentStatusType":
                    ref.setCode(code);
                    break;
                case "hemishe_HEducationYear":
                    ref.setCode(code);
                    ref.setNameRu((String) row.get("name_ru"));
                    ref.setNameEn((String) row.get("name_en"));
                    break;
                case "hemishe_HHemisVersionType":
                case "hemishe_HUniversityContractCategory":
                    ref.setCode(code);
                    ref.setNameRu((String) row.get("name_ru"));
                    ref.setActive(getBoolean(row, "active"));
                    ref.setNameEn((String) row.get("name_en"));
                    break;
                default:
                    ref.setCode(code);
                    break;
            }

            // OLD-HEMIS service endpoints include version in ALL classifiers
            ref.setVersion(getInteger(row, "version"));

            return ref;
        } catch (Exception e) {
            log.debug("Failed to load reference {}.{}: {}", tableName, code, e.getMessage());
            return null;
        }
    }

    private Boolean getBoolean(Map<String, Object> row, String key) {
        Object val = row.get(key);
        return (val instanceof Boolean b) ? b : null;
    }

    private Integer getInteger(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        return null;
    }
}
