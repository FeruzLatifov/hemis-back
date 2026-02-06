package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.common.dto.university.UniversityDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.University;
import uz.hemis.service.legacy.CubaNestedObjectLoader;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;
import uz.hemis.service.university.UniversityService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * University Service Controller - CUBA REST API Compatible
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/university")
@Tag(name = "67.OTM Config", description = "Universitet konfiguratsiyasi xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class UniversityServiceController {

    private final UniversityService universityService;
    private final UniversityRefLegacyService universityRefLegacyService;
    private final CubaNestedObjectLoader nestedObjectLoader;
    private final LegacySecurityHelper securityHelper;

    private static final String UNIVERSITY_ENTITY_NAME = "hemishe_EUniversity";

    @GetMapping("/config")
    @Operation(summary = "Get university configuration")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getConfig() {
        log.info("[CUBA Service] university/config");

        String universityCode = securityHelper.getUniversityCodeFromContext();

        if (universityCode == null) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("universityCode", null);
            return ResponseEntity.ok(result);
        }

        try {
            UniversityDto uni = universityService.findByCode(universityCode);

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

    @GetMapping("/get")
    @Operation(summary = "Get university by code")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getByCode(
            @Parameter(description = "University code", required = true)
            @RequestParam String code) {
        log.info("[CUBA Service] university/get: code={}", code);

        University u = universityRefLegacyService.findUniversityById(code).orElse(null);
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

    private Map<String, Object> buildUniversityMap(University u) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("_entityName", UNIVERSITY_ENTITY_NAME);
        item.put("_instanceName", u.getCode() + "-" + u.getName());
        item.put("id", u.getCode());

        if (u.getStudentUrl() != null) item.put("studentUrl", u.getStudentUrl());
        item.put("code", u.getCode());

        if (u.getSoato() != null) {
            Map<String, Object> soatoObj = nestedObjectLoader.loadSoato(u.getSoato());
            if (soatoObj != null) item.put("soato", soatoObj);
        }

        if (u.getUniversityActivityStatus() != null) {
            item.put("universityActivityStatus", nestedObjectLoader.loadClassifier(
                "hemishe_h_university_activity_status", "HUniversityActivityStatus", u.getUniversityActivityStatus()));
        }

        if (u.getUniversityType() != null) {
            item.put("universityType", nestedObjectLoader.loadClassifier(
                "hemishe_h_university_type", "HUniversityType", u.getUniversityType()));
        }

        if (u.getTin() != null) item.put("tin", u.getTin());

        if (u.getSoatoRegion() != null) {
            Map<String, Object> soatoRegionObj = nestedObjectLoader.loadSoato(u.getSoatoRegion());
            if (soatoRegionObj != null) item.put("soatoRegion", soatoRegionObj);
        }

        if (u.getUniversityVersion() != null) {
            item.put("versionType", nestedObjectLoader.loadClassifierWithNames(
                "hemishe_h_hemis_version_type", "HHemisVersionType", u.getUniversityVersion()));
        }

        item.put("addStudent", u.getAddStudent() != null ? u.getAddStudent() : true);
        if (u.getAddress() != null) item.put("address", u.getAddress());
        item.put("accreditationEdit", u.getAccreditationEdit() != null ? u.getAccreditationEdit() : false);
        item.put("active", u.getActive() != null ? u.getActive() : false);

        if (u.getUniversityContractCategory() != null) {
            item.put("universityContractCategory", nestedObjectLoader.loadClassifierWithNames(
                "hemishe_h_university_contract_category", "HUniversityContractCategory", u.getUniversityContractCategory()));
        }

        item.put("version", u.getVersion() != null ? u.getVersion() : 1);
        item.put("oneId", u.getOneId() != null ? u.getOneId() : true);
        item.put("allowGrouping", u.getAllowGrouping() != null ? u.getAllowGrouping() : true);
        if (u.getTeacherUrl() != null) item.put("teacherUrl", u.getTeacherUrl());
        item.put("allowTransferOutside", u.getAllowTransferOutside() != null ? u.getAllowTransferOutside() : true);

        if (u.getOwnership() != null) {
            item.put("ownership", nestedObjectLoader.loadClassifier(
                "hemishe_h_ownership", "HOwnership", u.getOwnership()));
        }

        item.put("name", u.getName());
        item.put("gpaEdit", u.getGpaEdit() != null ? u.getGpaEdit() : false);

        if (u.getUniversityBelongsTo() != null) {
            item.put("belongsTo", nestedObjectLoader.loadClassifier(
                "hemishe_h_university_belongs_to", "HUniversityBelongsTo", u.getUniversityBelongsTo()));
        }

        return item;
    }
}
