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

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
@Tag(name = "OTM", description = "Oliy ta'lim muassasasi ma'lumotlari xizmatlari")
@RestController
@RequestMapping("/services/otm")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class OtmServiceController {

    private final OtmIntegrationService otmIntegrationService;

    /**
     * Get student info by ID
     *
     * <p><strong>Endpoint:</strong> GET /services/otm/studentInfoById</p>
     *
     * @param studentId Student UUID
     * @return Student academic information
     */
    @Operation(
        summary = "Talaba ma'lumotlarini ID bo'yicha olish",
        description = "Talaba UUID raqami orqali to'liq akademik ma'lumotlarini olish"
    )
    @GetMapping("/studentInfoById")
    public ResponseEntity<Map<String, Object>> getStudentInfoById(
        @Parameter(description = "Talaba UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam UUID studentId
    ) {
        log.info("GET /services/otm/studentInfoById - studentId: {}", studentId);
        return ResponseEntity.ok(otmIntegrationService.getStudentInfoById(studentId));
    }

    /**
     * Get student info by PINFL
     *
     * <p><strong>Endpoint:</strong> GET /services/otm/studentInfoByPinfl</p>
     *
     * @param pinfl Student PINFL
     * @return Student academic information
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
        return ResponseEntity.ok(otmIntegrationService.getStudentInfoByPinfl(pinfl));
    }

    /**
     * Get student list by tutor
     *
     * <p><strong>Endpoint:</strong> GET /services/otm/studentListByTutor</p>
     *
     * @param tutorId Tutor (academic advisor) ID
     * @return List of students assigned to this tutor
     */
    @Operation(
        summary = "Tutor talabalarini olish",
        description = "Akademik maslahatchi (tutor) ID si bo'yicha uning talabalari ro'yxatini olish"
    )
    @GetMapping("/studentListByTutor")
    public ResponseEntity<List<Map<String, Object>>> getStudentListByTutor(
        @Parameter(description = "Tutor UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam UUID tutorId
    ) {
        log.info("GET /services/otm/studentListByTutor - tutorId: {}", tutorId);
        return ResponseEntity.ok(otmIntegrationService.getStudentListByTutor(tutorId));
    }
}
