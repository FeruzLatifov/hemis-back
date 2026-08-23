package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.student.StudentGpa;
import uz.hemis.service.student.StudentGpaService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * StudentGpa Entity Controller (CUBA Pattern)
 * Tag 04: Talaba GPA
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_EStudentGpa</p>
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>GET /app/rest/v2/entities/hemishe_EStudentGpa - List all GPA records</li>
 *   <li>POST /app/rest/v2/entities/hemishe_EStudentGpa - Create new GPA record</li>
 * </ul>
 *
 * <p>Response format (view=eStudentGpa-view):</p>
 * <pre>
 * [
 *   {
 *     "_entityName": "hemishe_EStudentGpa",
 *     "_instanceName": "...",
 *     "id": "UUID",
 *     "debtSubjects": 0,
 *     "method": "one_year",
 *     "level": { "_entityName": "hemishe_HCourse", "id", "code", "name" },
 *     "creditSum": "47.0",
 *     "subjects": 11,
 *     "educationYear": { "_entityName": "hemishe_HEducationYear", "id", "name" },
 *     "studentId": { "_entityName": "hemishe_EStudent", "id", "lastname", "firstname", "fathername" },
 *     "gpa": "4.0"
 *   }
 * ]
 * </pre>
 *
 * @since 1.0.0
 */
@Tag(name = "04.Talaba", description = "Talaba GPA ma'lumotlari API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentGpa")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentGpaEntityController {

    private final StudentGpaService studentGpaService;

    /**
     * Barcha GPA yozuvlarini olish (pagination bilan)
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/entities/hemishe_EStudentGpa}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * @param view  View nomi (default: eStudentGpa-view)
     * @param limit Maksimum natijalar soni
     * @param offset Boshlang'ich pozitsiya
     * @return GPA yozuvlari ro'yxati (CUBA format)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(
            summary = "Talaba GPA ro'yxatini olish",
            description = """
                Barcha talabalarning GPA (Grade Point Average) ma'lumotlarini olish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** GET /app/rest/v2/entities/hemishe_EStudentGpa
                **Auth:** Bearer token (required)

                **Parametrlar:**
                - view: View nomi (default: eStudentGpa-view)
                - limit: Maksimum natijalar soni (default: 100)
                - offset: Boshlang'ich pozitsiya (default: 0)

                **Response:** GPA yozuvlari ro'yxati (CUBA format)
                - _entityName: "hemishe_EStudentGpa"
                - _instanceName: Instance nomi
                - id: GPA yozuv UUID
                - gpa: GPA qiymati (masalan: "4.0")
                - method: Hisoblash usuli ("one_year" yoki "all_year")
                - level: Kurs darajasi (hemishe_HCourse)
                - creditSum: Jami kredit
                - subjects: Fan soni
                - debtSubjects: Qarzdor fan soni
                - educationYear: Ta'lim yili
                - studentId: Talaba ma'lumotlari
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - GPA ro'yxati qaytarildi",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "View nomi")
            @RequestParam(value = "view", required = false) String view,
            @Parameter(description = "Maksimum natijalar soni")
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
            @Parameter(description = "Boshlang'ich pozitsiya")
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {

        log.info("[CUBA Entity] hemishe_EStudentGpa: view={}, limit={}, offset={}", view, limit, offset);

        List<Map<String, Object>> result = studentGpaService.findAll(limit, offset, view);

        log.info("[CUBA Entity] hemishe_EStudentGpa: {} records returned", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Yangi GPA yozuvini yaratish
     *
     * <p><strong>URL:</strong> {@code POST /app/rest/v2/entities/hemishe_EStudentGpa}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * <p><strong>Request Body format:</strong></p>
     * <pre>
     * {
     *   "studentId": {"id": "UUID"},     // Talaba UUID
     *   "educationYear": {"code": "2023"}, // Ta'lim yili kodi
     *   "level": {"code": "12"},         // Kurs darajasi kodi
     *   "gpa": "4.0",                    // GPA qiymati
     *   "method": "one_year",            // Hisoblash usuli
     *   "creditSum": "47.0",             // Jami kredit
     *   "subjects": 11,                  // Fan soni
     *   "debtSubjects": 0                // Qarzdor fan soni
     * }
     * </pre>
     *
     * @param requestBody GPA ma'lumotlari (CUBA entity format)
     * @return Yaratilgan GPA yozuvi (CUBA format)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @Operation(
            summary = "Yangi GPA yozuvini yaratish",
            description = """
                Yangi GPA (Grade Point Average) yozuvini yaratish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** POST /app/rest/v2/entities/hemishe_EStudentGpa
                **Auth:** Bearer token (required)
                **Content-Type:** application/json

                **Request Body:**
                ```json
                {
                  "studentId": {"id": "UUID"},     // Talaba UUID (required)
                  "educationYear": {"code": "2023"}, // Ta'lim yili kodi
                  "level": {"code": "12"},         // Kurs darajasi kodi
                  "gpa": "4.0",                    // GPA qiymati
                  "method": "one_year",            // Hisoblash usuli (one_year/all_year)
                  "creditSum": "47.0",             // Jami kredit
                  "subjects": 11,                  // Fan soni
                  "debtSubjects": 0                // Qarzdor fan soni
                }
                ```

                **Response:** Yaratilgan GPA yozuvi (CUBA format)
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Muvaffaqiyatli - GPA yozuvi yaratildi",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri ma'lumotlar"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GPA ma'lumotlari (CUBA entity format)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
            @RequestBody Map<String, Object> requestBody) {

        log.info("[CUBA Entity] POST hemishe_EStudentGpa: {}", requestBody);

        Map<String, Object> result = studentGpaService.create(requestBody);

        log.info("[CUBA Entity] POST hemishe_EStudentGpa: CREATED with ID {}", result.get("id"));
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{entityId}")
    @Operation(summary = "GPA yozuvini ID bo'yicha olish")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId,
            @RequestParam(value = "view", required = false) String view) {
        log.info("[CUBA Entity] GET hemishe_EStudentGpa/{}", entityId);
        Map<String, Object> result = studentGpaService.findById(entityId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{entityId}")
    @Operation(summary = "GPA yozuvini yangilash")
    public ResponseEntity<Map<String, Object>> update(@PathVariable UUID entityId,
            @RequestParam(value = "responseView", required = false) String responseView,
            @RequestBody Map<String, Object> requestBody) {
        log.info("[CUBA Entity] PUT hemishe_EStudentGpa/{}: {}", entityId, requestBody);
        Optional<StudentGpa> existing = studentGpaService.findEntityById(entityId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        requestBody.put("id", entityId.toString());
        Map<String, Object> result = studentGpaService.create(requestBody);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{entityId}")
    @Operation(summary = "GPA yozuvini o'chirish")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("[CUBA Entity] DELETE hemishe_EStudentGpa/{}", entityId);
        Optional<StudentGpa> existing = studentGpaService.findEntityById(entityId);
        if (existing.isEmpty()) {
            Map<String, Object> error = new java.util.LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity hemishe_EStudentGpa with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }
        studentGpaService.delete(existing.get());
        return ResponseEntity.ok().build();
    }
}
