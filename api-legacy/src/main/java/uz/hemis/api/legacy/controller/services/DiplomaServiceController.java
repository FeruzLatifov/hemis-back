package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.repository.DiplomaRepository;

import java.util.*;

/**
 * Diploma Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/diploma/*}</p>
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Matches OLD-HEMIS CUBA service pattern</li>
 *   <li>200+ universities depend on /byhash endpoint for QR diploma verification</li>
 *   <li>Used by employers and government agencies for diploma authentication</li>
 * </ul>
 *
 * <p><strong>Methods:</strong></p>
 * <ul>
 *   <li>info - Get diploma by number</li>
 *   <li>byhash - Verify diploma by QR hash</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "Diploma Service API", description = "CUBA compatible diploma verification and information service endpoints")
@RestController
@RequestMapping("/services/diploma")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DiplomaServiceController {

    private final DiplomaRepository diplomaRepository;

    /**
     * Get diploma info
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/diploma/info</p>
     *
     * @param number diploma number
     * @return diploma information
     */
    @GetMapping("/info")
    @Operation(summary = "Get diploma info", description = "Returns diploma information by number")
    public ResponseEntity<Map<String, Object>> info(
            @Parameter(description = "Diploma number")
            @RequestParam String number
    ) {
        log.info("GET /services/diploma/info - number: {}", number);

        return diplomaRepository.findByDiplomaNumber(number)
                .map(diploma -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("id", diploma.getId());
                    result.put("diplomaNumber", diploma.getDiplomaNumber());
                    result.put("serialNumber", diploma.getSerialNumber());
                    result.put("student", diploma.getStudent());
                    result.put("university", diploma.getUniversity());
                    result.put("specialty", diploma.getSpecialty());
                    result.put("diplomaType", diploma.getDiplomaType());
                    result.put("issueDate", diploma.getIssueDate());
                    result.put("registrationDate", diploma.getRegistrationDate());
                    result.put("graduationYear", diploma.getGraduationYear());
                    result.put("qualification", diploma.getQualification());
                    result.put("averageGrade", diploma.getAverageGrade());
                    result.put("honors", diploma.getHonors());
                    result.put("diplomaHash", diploma.getDiplomaHash());
                    result.put("rectorName", diploma.getRectorName());
                    result.put("status", diploma.getStatus());
                    result.put("qrCode", diploma.getQrCode());
                    result.put("verificationUrl", diploma.getVerificationUrl());
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get diploma by hash (QR code verification)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/diploma/byhash</p>
     *
     * <p>This endpoint is used by employers and government agencies to verify diploma authenticity
     * by scanning the QR code on the diploma.</p>
     *
     * <p><strong>Example Response:</strong></p>
     * <pre>
     * {
     *   "id": "uuid",
     *   "number": "12345678",
     *   "series": "AB",
     *   "studentFullName": "Aliyev Ali Alievich",
     *   "studentPinfl": "12345678901234",
     *   "universityName": "Toshkent Davlat Texnika Universiteti",
     *   "specialtyName": "Dasturiy injiniring (5140700)",
     *   "issueDate": "2024-06-15",
     *   "diplomaHash": "71d6a9e0436cfb3aaa9fee3f88844b42",
     *   "status": "ACTIVE",
     *   "verified": true
     * }
     * </pre>
     *
     * @param hash diploma hash from QR code
     * @return diploma information or 404 if not found
     */
    @GetMapping("/byhash")
    @Operation(
            summary = "Get diploma by hash",
            description = "Verifies diploma authenticity using QR code hash. Used by employers and government agencies."
    )
    public ResponseEntity<Map<String, Object>> byHash(
            @Parameter(description = "Diploma hash from QR code", example = "71d6a9e0436cfb3aaa9fee3f88844b42")
            @RequestParam String hash
    ) {
        log.info("GET /services/diploma/byhash - hash: {}", hash);

        return diplomaRepository.findByDiplomaHash(hash)
                .map(diploma -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("id", diploma.getId());
                    result.put("diplomaNumber", diploma.getDiplomaNumber());
                    result.put("serialNumber", diploma.getSerialNumber());
                    result.put("student", diploma.getStudent());
                    result.put("university", diploma.getUniversity());
                    result.put("specialty", diploma.getSpecialty());
                    result.put("diplomaType", diploma.getDiplomaType());
                    result.put("issueDate", diploma.getIssueDate());
                    result.put("registrationDate", diploma.getRegistrationDate());
                    result.put("graduationYear", diploma.getGraduationYear());
                    result.put("qualification", diploma.getQualification());
                    result.put("averageGrade", diploma.getAverageGrade());
                    result.put("honors", diploma.getHonors());
                    result.put("diplomaHash", diploma.getDiplomaHash());
                    result.put("rectorName", diploma.getRectorName());
                    result.put("status", diploma.getStatus());
                    result.put("qrCode", diploma.getQrCode());
                    result.put("verificationUrl", diploma.getVerificationUrl());
                    result.put("verified", true);
                    result.put("verificationDate", new Date());

                    log.info("Diploma verified successfully - number: {}, hash: {}", diploma.getDiplomaNumber(), hash);
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> {
                    log.warn("Diploma not found for hash: {}", hash);
                    return ResponseEntity.notFound().build();
                });
    }
}
