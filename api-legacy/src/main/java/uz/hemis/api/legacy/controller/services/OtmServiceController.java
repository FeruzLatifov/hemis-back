package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.OtmIntegrationService;

import java.util.LinkedHashMap;
import java.util.Map;

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
@RequestMapping("/services/otm")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class OtmServiceController {

    private final OtmIntegrationService otmIntegrationService;

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
    @GetMapping("/studentInfoById")
    public ResponseEntity<Map<String, Object>> getStudentInfoById(
        @Parameter(description = "Talaba ID (string format)", required = true, example = "999221100044")
        @RequestParam String studentId
    ) {
        log.info("GET /services/otm/studentInfoById - studentId: {}", studentId);
        Map<String, Object> data = otmIntegrationService.getStudentInfoById(studentId);
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
    @GetMapping("/studentInfoByPinfl")
    public ResponseEntity<Map<String, Object>> getStudentInfoByPinfl(
        @Parameter(description = "PINFL", required = true, example = "31503776560016")
        @RequestParam String pinfl
    ) {
        log.info("GET /services/otm/studentInfoByPinfl - pinfl: {}", pinfl);
        Map<String, Object> data = otmIntegrationService.getStudentInfoByPinfl(pinfl);
        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get student list by tutor
     *
     * <p><strong>Endpoint:</strong> GET /services/otm/studentListByTutor</p>
     * <p><strong>OLD-HEMIS format:</strong> university=999&tutorPinfl=31503776560016</p>
     *
     * @param university University code (e.g., "999")
     * @param tutorPinfl Tutor PINFL (e.g., "31503776560016")
     * @return List of students assigned to this tutor wrapped in {success, data}
     */
    @Operation(
        summary = "Tutor talabalarini olish",
        description = "Universitet kodi va tutor PINFL raqami bo'yicha talabalar ro'yxatini olish. " +
                      "OLD-HEMIS format: university=999&tutorPinfl=31503776560016"
    )
    @GetMapping("/studentListByTutor")
    public ResponseEntity<Map<String, Object>> getStudentListByTutor(
        @Parameter(description = "Universitet kodi", required = true, example = "999")
        @RequestParam String university,
        @Parameter(description = "Tutor PINFL", required = true, example = "31503776560016")
        @RequestParam String tutorPinfl
    ) {
        log.info("GET /services/otm/studentListByTutor - university: {}, tutorPinfl: {}", university, tutorPinfl);
        Object data = otmIntegrationService.getStudentListByTutor(university, tutorPinfl);
        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Wraps response data in {success, data} format for OLD-HEMIS compatibility.
     * Uses LinkedHashMap to ensure consistent field order (success first, then data).
     *
     * @param data The data to wrap
     * @return LinkedHashMap with "success" and "data" keys
     */
    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
}
