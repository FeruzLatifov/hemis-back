package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.LegacyOtmIntegrationService;

import uz.hemis.api.legacy.util.LegacySecurityHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * OTM Service Controller - University Information Services
 *
 * <p><strong>URL Pattern:</strong> {@code /services/otm/*}</p>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Provides student information for external systems</li>
 *   <li>Used by government agencies and partner universities</li>
 *   <li>Student academic data export</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "64.OTM", description = "Oliy ta'lim muassasasi ma'lumotlari xizmatlari")
@RestController
@RequestMapping("/app/rest/v2/services/otm")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class OtmServiceController {

    private final LegacyOtmIntegrationService otmIntegrationService;
    private final LegacySecurityHelper securityHelper;

    /**
     * Get student info by ID (student_id string format from OLD-HEMIS)
     *
     * <p><strong>Endpoint:</strong> GET /services/otm/studentInfoById</p>
     * <p><strong>OLD-HEMIS format:</strong> studentId=999221100044 (String, NOT UUID)</p>
     *
     * @param studentId Student ID in string format (e.g., "999221100044")
     * @return Student academic information wrapped in {success, data}
     */
    @Operation(
        summary = "Talaba ma'lumotlarini ID bo'yicha olish",
        description = "Talaba ID raqami (string format) orqali to'liq akademik ma'lumotlarini olish. " +
                      "OLD-HEMIS format: studentId=999221100044"
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/studentInfoById")
    public ResponseEntity<Map<String, Object>> getStudentInfoById(
        @Parameter(description = "Talaba ID (string format)", required = true)
        @RequestParam String studentId
    ) {
        log.info("GET /services/otm/studentInfoById - studentId: {}", studentId);
        Map<String, Object> data = otmIntegrationService.getStudentInfoById(studentId);
        if (data == null) {
            return ResponseEntity.ok(wrapErrorResponse("Student not found!"));
        }
        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get student info by PINFL
     *
     * <p><strong>Endpoint:</strong> GET /services/otm/studentInfoByPinfl</p>
     *
     * @param pinfl Student PINFL
     * @return Student academic information wrapped in {success, data}
     */
    @Operation(
        summary = "Talaba ma'lumotlarini PINFL bo'yicha olish",
        description = "Talaba PINFL raqami orqali akademik ma'lumotlarini olish"
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/studentInfoByPinfl")
    public ResponseEntity<Map<String, Object>> getStudentInfoByPinfl(
        @Parameter(description = "PINFL", required = true)
        @RequestParam String pinfl
    ) {
        log.info("GET /services/otm/studentInfoByPinfl - pinfl: {}", uz.hemis.common.log.LogSafe.pinfl(pinfl));
        String universityCode = securityHelper.getUniversityCodeFromContext();
        Map<String, Object> data = otmIntegrationService.getStudentInfoByPinfl(pinfl, universityCode);
        if (data == null) {
            return ResponseEntity.ok(wrapErrorResponse("Student not found!"));
        }
        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get student list by tutor
     *
     * <p><strong>Endpoint:</strong> GET /services/otm/studentListByTutor</p>
     * <p><strong>OLD-HEMIS format:</strong> university=999&tutorPinfl=00000000000000</p>
     *
     * @param university University code (e.g., "999")
     * @param tutorPinfl Tutor PINFL (e.g., "00000000000000")
     * @return List of students assigned to this tutor wrapped in {success, data}
     */
    @Operation(
        summary = "Tutor talabalarini olish",
        description = "Universitet kodi va tutor PINFL raqami bo'yicha talabalar ro'yxatini olish. " +
                      "OLD-HEMIS format: university=999&tutorPinfl=00000000000000"
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/studentListByTutor")
    public ResponseEntity<Map<String, Object>> getStudentListByTutor(
        @Parameter(description = "Universitet kodi", required = true)
        @RequestParam String university,
        @Parameter(description = "Tutor PINFL", required = true)
        @RequestParam String tutorPinfl
    ) {
        log.info("GET /services/otm/studentListByTutor - university: {}, tutorPinfl: {}", university, tutorPinfl);
        Object data = otmIntegrationService.getStudentListByTutor(university, tutorPinfl);
        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Wraps response data in {success, data, code} format for OLD-HEMIS compatibility.
     * Uses LinkedHashMap to ensure consistent field order.
     *
     * @param data The data to wrap
     * @return LinkedHashMap with "success", "data", and "code" keys
     */
    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("code", 200);
        return response;
    }

    /**
     * Wraps error response in OLD-HEMIS format: {success: false, message, code: "404"}
     * <p>Old-hemis 1:1 mosligi — field nomi {@code message} (error emas), code string ("404").</p>
     */
    private Map<String, Object> wrapErrorResponse(String errorMessage) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("message", errorMessage);
        response.put("code", "404");
        return response;
    }
}
