package uz.hemis.api.legacy.controller.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.document.DiplomaService;

import java.util.*;

/**
 * Diploma Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/diploma/*}</p>
 *
 * @since 2.0.0
 */
@Tag(name = "70.Qo'shimcha xizmatlar", description = "Diploma tekshirish va ma'lumotlar xizmatlari")
@RestController
@RequestMapping("/app/rest/v2/services/diploma")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DiplomaServiceController {

    private final DiplomaService diplomaService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Get diploma info by PINFL
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/diploma/info?pinfl=...</p>
     */
    @GetMapping("/info")
    @Operation(summary = "Diploma ma'lumotlarini PINFL orqali olish")
    public ResponseEntity<?> info(
            @Parameter(description = "Talabaning PINFL raqami")
            @RequestParam String pinfl
    ) {
        log.info("GET /services/diploma/info - pinfl: {}****", pinfl.length() > 4 ? pinfl.substring(0, 4) : pinfl);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT d.id, d.diploma_number, d.register_number, d.register_date, " +
                "d._speciality, d.speciality_name, d._university, d.active, d._diplom_category, " +
                "d._education_type, d._education_year, d.total_credit, d.total_acload, d.avg_grade, " +
                "s.firstname, s.lastname, s.fathername, s.pinfl " +
                "FROM hemishe_e_student_diploma d " +
                "JOIN hemishe_e_student s ON s.id = d._student " +
                "WHERE s.pinfl = ? AND d.delete_ts IS NULL AND s.delete_ts IS NULL " +
                "ORDER BY d.create_ts DESC",
                pinfl
        );

        if (rows.isEmpty()) {
            // OLD-HEMIS format: wraps error inside data array
            Map<String, Object> innerError = new LinkedHashMap<>();
            innerError.put("success", false);
            innerError.put("error", pinfl + " JSHSHIRga ega talaba uchun diplom topilmadi");
            innerError.put("data", List.of());
            innerError.put("code", 404);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("status", 200);
            response.put("count", 1);
            response.put("data", List.of(innerError));
            return ResponseEntity.ok(response);
        }

        List<Map<String, Object>> diplomas = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("_entityName", "hemishe_EStudentDiploma");
            map.put("id", row.get("id"));
            map.put("diplomaNumber", row.get("diploma_number"));
            map.put("registerNumber", row.get("register_number"));
            map.put("registerDate", row.get("register_date"));
            map.put("speciality", row.get("_speciality"));
            map.put("specialityName", row.get("speciality_name"));
            map.put("university", row.get("_university"));
            map.put("active", row.get("active"));
            map.put("diplomCategory", row.get("_diplom_category"));
            map.put("educationType", row.get("_education_type"));
            map.put("educationYear", row.get("_education_year"));
            map.put("totalCredit", row.get("total_credit"));
            map.put("totalAcload", row.get("total_acload"));
            map.put("avgGrade", row.get("avg_grade"));

            Map<String, Object> student = new LinkedHashMap<>();
            student.put("firstname", row.get("firstname"));
            student.put("lastname", row.get("lastname"));
            student.put("fathername", row.get("fathername"));
            student.put("pinfl", row.get("pinfl"));
            map.put("student", student);

            diplomas.add(map);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", 200);
        response.put("count", diplomas.size());
        response.put("data", diplomas);

        return ResponseEntity.ok(response);
    }

    /**
     * Get diploma by hash (QR code verification)
     *
     * <p><strong>Legacy Endpoint:</strong> GET /app/rest/v2/services/diploma/byhash</p>
     */
    @GetMapping("/byhash")
    @Operation(
            summary = "Get diploma by hash",
            description = "Verifies diploma authenticity using QR code hash."
    )
    public ResponseEntity<Map<String, Object>> byHash(
            @Parameter(description = "Diploma hash from QR code")
            @RequestParam String hash
    ) {
        log.info("GET /services/diploma/byhash - hash: {}", hash);

        return diplomaService.findEntityByDiplomaHash(hash)
                .map(diploma -> {
                    log.info("Diploma verified - number: {}", diploma.getDiplomaNumber());
                    return ResponseEntity.ok(diplomaService.toDiplomaVerificationMap(diploma));
                })
                .orElseGet(() -> {
                    log.warn("Diploma not found for hash: {}", hash);
                    return ResponseEntity.notFound().build();
                });
    }
}
