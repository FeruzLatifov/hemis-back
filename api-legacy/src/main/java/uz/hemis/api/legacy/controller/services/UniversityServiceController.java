package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.UniversityDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.SecUser;
import uz.hemis.domain.entity.University;
import uz.hemis.domain.repository.SecUserRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.UniversityService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * University Service Controller - CUBA REST API Compatible
 *
 * <p>Preserves exact URLs from old HEMIS CUBA platform for backward compatibility</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/university/*}</p>
 *
 * <p><strong>Methods:</strong></p>
 * <ul>
 *   <li>config - Get university configuration for authenticated user</li>
 *   <li>get - Get university by code</li>
 * </ul>
 *
 * @since 2.0.0
 * @see UniversityService
 */
@RestController
@RequestMapping("/app/rest/v2/services/university")
@Tag(name = "67.OTM Config", description = "Universitet konfiguratsiyasi xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class UniversityServiceController {

    private final UniversityService universityService;
    private final SecUserRepository secUserRepository;
    private final UniversityRepository universityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String UNIVERSITY_ENTITY_NAME = "hemishe_EUniversity";

    /**
     * Get university configuration for authenticated user
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/university/config}</p>
     *
     * <p>OLD-HEMIS format:</p>
     * <pre>
     * {
     *   "success": true,
     *   "universityCode": "401",
     *   "gpaEdit": false,
     *   "accreditationEdit": false,
     *   "addStudent": true,
     *   "allowGrouping": true,
     *   "allowTransferOutside": true,
     *   "oneId": true
     * }
     * </pre>
     *
     * @return university configuration data
     */
    @GetMapping("/config")
    @Operation(
        summary = "Get university configuration",
        description = "Returns university configuration for authenticated user"
    )
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getConfig() {
        log.info("[CUBA Service] university/config");

        // Authenticated user dan university code olish
        String universityCode = getAuthenticatedUserUniversityCode();

        if (universityCode == null) {
            // System admin — university yo'q
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("universityCode", null);
            return ResponseEntity.ok(result);
        }

        try {
            UniversityDto uni = universityService.findByCode(universityCode);

            // Old-hemis format
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("universityCode", uni.getCode());
            result.put("gpaEdit", uni.getGpaEdit() != null ? uni.getGpaEdit() : false);
            result.put("accreditationEdit", uni.getAccreditationEdit() != null ? uni.getAccreditationEdit() : false);
            result.put("addStudent", uni.getAddStudent() != null ? uni.getAddStudent() : true);
            result.put("allowGrouping", uni.getAllowGrouping() != null ? uni.getAllowGrouping() : true);
            result.put("allowTransferOutside", uni.getAllowTransferOutside() != null ? uni.getAllowTransferOutside() : true);
            result.put("oneId", true);

            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("message", "University not found: " + universityCode);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Get university by code
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/university/get}</p>
     *
     * <p>OLD-HEMIS format:</p>
     * <pre>
     * {
     *   "success": true,
     *   "university": {
     *     "_entityName": "hemishe_EUniversity",
     *     "id": "401",
     *     "code": "401",
     *     "name": "...",
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param code university code
     * @return university details in old-hemis format
     */
    @GetMapping("/get")
    @Operation(
        summary = "Get university by code",
        description = "Returns detailed university information by unique code"
    )
    @PreAuthorize("permitAll()")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getByCode(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String code) {
        log.info("[CUBA Service] university/get: code={}", code);

        University u = universityRepository.findById(code).orElse(null);
        if (u == null) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("message", "University not found: " + code);
            return ResponseEntity.ok(result);
        }

        Map<String, Object> universityMap = buildUniversityMap(u);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("university", universityMap);

        return ResponseEntity.ok(result);
    }

    /**
     * University entity ni OLD-HEMIS CUBA formatiga o'girish
     * camelCase field nomlari va nested reference objectlar
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildUniversityMap(University u) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("_entityName", UNIVERSITY_ENTITY_NAME);
        item.put("_instanceName", u.getCode() + "-" + u.getName());
        item.put("id", u.getCode());

        if (u.getStudentUrl() != null) item.put("studentUrl", u.getStudentUrl());
        item.put("code", u.getCode());

        // soato nested object
        if (u.getSoato() != null) {
            Map<String, Object> soatoObj = loadSoatoObject(u.getSoato());
            if (soatoObj != null) item.put("soato", soatoObj);
        }

        // universityActivityStatus nested object
        if (u.getUniversityActivityStatus() != null) {
            item.put("universityActivityStatus", loadClassifierObject(
                "hemishe_h_university_activity_status", "HUniversityActivityStatus", u.getUniversityActivityStatus()));
        }

        // universityType nested object
        if (u.getUniversityType() != null) {
            item.put("universityType", loadClassifierObject(
                "hemishe_h_university_type", "HUniversityType", u.getUniversityType()));
        }

        if (u.getTin() != null) item.put("tin", u.getTin());

        // soatoRegion nested object
        if (u.getSoatoRegion() != null) {
            Map<String, Object> soatoRegionObj = loadSoatoObject(u.getSoatoRegion());
            if (soatoRegionObj != null) item.put("soatoRegion", soatoRegionObj);
        }

        // versionType nested object
        String versionTypeCode = getUniversityVersionType(u.getCode());
        if (versionTypeCode != null) {
            item.put("versionType", loadClassifierObjectWithNames(
                "hemishe_h_hemis_version_type", "HHemisVersionType", versionTypeCode));
        }

        item.put("addStudent", u.getAddStudent() != null ? u.getAddStudent() : true);
        if (u.getAddress() != null) item.put("address", u.getAddress());
        item.put("accreditationEdit", u.getAccreditationEdit() != null ? u.getAccreditationEdit() : false);
        item.put("active", u.getActive() != null ? u.getActive() : false);

        // universityContractCategory nested object
        if (u.getUniversityContractCategory() != null) {
            item.put("universityContractCategory", loadClassifierObjectWithNames(
                "hemishe_h_university_contract_category", "HUniversityContractCategory", u.getUniversityContractCategory()));
        }

        item.put("version", u.getVersion() != null ? u.getVersion() : 1);
        item.put("oneId", u.getOneId() != null ? u.getOneId() : true);
        item.put("allowGrouping", u.getAllowGrouping() != null ? u.getAllowGrouping() : true);
        if (u.getTeacherUrl() != null) item.put("teacherUrl", u.getTeacherUrl());
        item.put("allowTransferOutside", u.getAllowTransferOutside() != null ? u.getAllowTransferOutside() : true);

        // ownership nested object
        if (u.getOwnership() != null) {
            item.put("ownership", loadClassifierObject(
                "hemishe_h_ownership", "HOwnership", u.getOwnership()));
        }

        item.put("name", u.getName());
        item.put("gpaEdit", u.getGpaEdit() != null ? u.getGpaEdit() : false);

        // belongsTo nested object
        if (u.getUniversityBelongsTo() != null) {
            item.put("belongsTo", loadClassifierObject(
                "hemishe_h_university_belongs_to", "HUniversityBelongsTo", u.getUniversityBelongsTo()));
        }

        return item;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadClassifierObject(String tableName, String entityName, String code) {
        try {
            String sql = "SELECT code, name, active, version FROM " + tableName + " WHERE code = :code AND delete_ts IS NULL";
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter("code", code)
                    .getResultList();
            if (results.isEmpty()) return null;

            Object[] row = results.get(0);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", String.valueOf(row[0]));
            obj.put("code", String.valueOf(row[0]));
            obj.put("name", row[1] != null ? String.valueOf(row[1]) : "");
            obj.put("active", row[2] != null ? row[2] : true);
            obj.put("version", row[3] != null ? ((Number) row[3]).intValue() : 1);
            return obj;
        } catch (Exception e) {
            log.debug("Klassifikator yuklashda xatolik ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadClassifierObjectWithNames(String tableName, String entityName, String code) {
        try {
            String sql = "SELECT code, name, active, version, name_ru, name_en FROM " + tableName + " WHERE code = :code AND delete_ts IS NULL";
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter("code", code)
                    .getResultList();
            if (results.isEmpty()) return null;

            Object[] row = results.get(0);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", String.valueOf(row[0]));
            obj.put("code", String.valueOf(row[0]));
            obj.put("name", row[1] != null ? String.valueOf(row[1]) : "");
            obj.put("active", row[2] != null ? row[2] : true);
            obj.put("version", row[3] != null ? ((Number) row[3]).intValue() : 1);
            if (row[4] != null) obj.put("nameRu", String.valueOf(row[4]));
            if (row[5] != null) obj.put("nameEn", String.valueOf(row[5]));
            return obj;
        } catch (Exception e) {
            log.debug("Klassifikator yuklashda xatolik ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadSoatoObject(String soatoCode) {
        try {
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT code, name_uz, name_ru, active, version FROM hemishe_h_soato WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", soatoCode)
                    .getResultList();
            if (results.isEmpty()) return null;

            Object[] row = results.get(0);
            Map<String, Object> soato = new LinkedHashMap<>();
            soato.put("_entityName", "hemishe_HSoato");
            soato.put("id", row[0]);
            soato.put("code", row[0]);
            soato.put("active", row[3] != null ? row[3] : true);
            soato.put("version", row[4] != null ? ((Number) row[4]).intValue() : 1);
            soato.put("name_ru", row[2] != null ? String.valueOf(row[2]) : "");
            soato.put("name_uz", row[1] != null ? String.valueOf(row[1]) : "");
            return soato;
        } catch (Exception e) {
            log.debug("SOATO yuklashda xatolik (code={}): {}", soatoCode, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String getUniversityVersionType(String universityCode) {
        try {
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT _university_version FROM hemishe_e_university WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", universityCode)
                    .getResultList();
            if (!results.isEmpty() && results.get(0) != null) {
                return String.valueOf(results.get(0));
            }
        } catch (Exception e) {
            log.debug("versionType olishda xatolik (code={}): {}", universityCode, e.getMessage());
        }
        return null;
    }

    /**
     * Authenticated user ning university code ni olish
     */
    private String getAuthenticatedUserUniversityCode() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return null;

            // JWT dan username claim ni olish (auth.getName() UUID qaytaradi, login emas)
            String username = null;
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                username = jwt.getClaimAsString("username");
            }
            if (username == null) {
                username = auth.getName(); // fallback
            }

            log.debug("[CUBA Service] university/config: username={}", username);
            return secUserRepository.findByLoginAndActiveTrue(username)
                    .map(SecUser::getUniversityCode)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Cannot get user university code: {}", e.getMessage());
            return null;
        }
    }
}
