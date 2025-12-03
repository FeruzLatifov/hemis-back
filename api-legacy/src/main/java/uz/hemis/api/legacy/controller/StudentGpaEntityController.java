package uz.hemis.api.legacy.controller;

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
import uz.hemis.service.StudentGpaService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
 *   <li>GET /app/rest/v2/entities/hemishe_EStudentGpa/{id} - Get GPA by ID</li>
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
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "GPA ro'yxati",
                                    value = """
                                        [
                                          {
                                            "_entityName": "hemishe_EStudentGpa",
                                            "_instanceName": "com.company.hemishe.entity.EStudentGpa-... [detached]",
                                            "id": "ccb27f69-5258-0ae3-9e97-f068d65fadf9",
                                            "debtSubjects": 0,
                                            "method": "one_year",
                                            "level": {
                                              "_entityName": "hemishe_HCourse",
                                              "_instanceName": "12 2-kurs",
                                              "id": "12",
                                              "code": "12",
                                              "name": "2-kurs"
                                            },
                                            "creditSum": "47.0",
                                            "subjects": 11,
                                            "educationYear": {
                                              "_entityName": "hemishe_HEducationYear",
                                              "_instanceName": "2023-2024",
                                              "id": "2023",
                                              "name": "2023-2024"
                                            },
                                            "studentId": {
                                              "_entityName": "hemishe_EStudent",
                                              "_instanceName": "YULCHIYEVA GAVXAR ISLAMOVNA",
                                              "id": "ee2f9738-5992-2b63-a85f-9fa3d98862e5",
                                              "lastname": "YULCHIYEVA",
                                              "firstname": "GAVXAR",
                                              "fathername": "ISLAMOVNA"
                                            },
                                            "gpa": "4.0"
                                          }
                                        ]
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "View nomi", example = "eStudentGpa-view")
            @RequestParam(value = "view", required = false, defaultValue = "eStudentGpa-view") String view,
            @Parameter(description = "Maksimum natijalar soni", example = "100")
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
            @Parameter(description = "Boshlang'ich pozitsiya", example = "0")
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {

        log.info("[CUBA Entity] hemishe_EStudentGpa: view={}, limit={}, offset={}", view, limit, offset);

        List<Map<String, Object>> result = studentGpaService.findAll(limit, offset);

        log.info("[CUBA Entity] hemishe_EStudentGpa: {} records returned", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Bitta GPA yozuvini ID bo'yicha olish
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/entities/hemishe_EStudentGpa/{entityId}}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * @param entityId GPA yozuv UUID
     * @param view     View nomi (default: eStudentGpa-view)
     * @return GPA yozuvi (CUBA format)
     */
    @GetMapping("/{entityId}")
    @Operation(
            summary = "Bitta GPA yozuvini olish",
            description = """
                ID bo'yicha bitta GPA yozuvini olish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** GET /app/rest/v2/entities/hemishe_EStudentGpa/{entityId}
                **Auth:** Bearer token (required)

                **Parametrlar:**
                - entityId: GPA yozuv UUID (path variable)
                - view: View nomi (default: eStudentGpa-view)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - GPA yozuvi qaytarildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "GPA yozuvi topilmadi")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "GPA yozuv UUID", example = "ccb27f69-5258-0ae3-9e97-f068d65fadf9")
            @PathVariable UUID entityId,
            @Parameter(description = "View nomi", example = "eStudentGpa-view")
            @RequestParam(value = "view", required = false, defaultValue = "eStudentGpa-view") String view) {

        log.info("[CUBA Entity] hemishe_EStudentGpa/{}: view={}", entityId, view);

        Map<String, Object> result = studentGpaService.findById(entityId);

        if (result == null) {
            log.warn("[CUBA Entity] hemishe_EStudentGpa/{}: NOT FOUND", entityId);
            return ResponseEntity.notFound().build();
        }

        log.info("[CUBA Entity] hemishe_EStudentGpa/{}: FOUND", entityId);
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
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Yaratilgan GPA",
                                    value = """
                                        {
                                          "_entityName": "hemishe_EStudentGpa",
                                          "_instanceName": "com.company.hemishe.entity.EStudentGpa-... [detached]",
                                          "id": "generated-uuid",
                                          "debtSubjects": 0,
                                          "method": "one_year",
                                          "level": {
                                            "_entityName": "hemishe_HCourse",
                                            "_instanceName": "12 2-kurs",
                                            "id": "12",
                                            "code": "12",
                                            "name": "2-kurs"
                                          },
                                          "creditSum": "47.0",
                                          "subjects": 11,
                                          "educationYear": {
                                            "_entityName": "hemishe_HEducationYear",
                                            "_instanceName": "2023-2024",
                                            "id": "2023",
                                            "name": "2023-2024"
                                          },
                                          "studentId": {
                                            "_entityName": "hemishe_EStudent",
                                            "_instanceName": "YULCHIYEVA GAVXAR ISLAMOVNA",
                                            "id": "student-uuid",
                                            "lastname": "YULCHIYEVA",
                                            "firstname": "GAVXAR",
                                            "fathername": "ISLAMOVNA"
                                          },
                                          "gpa": "4.0"
                                        }
                                        """
                            )
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
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "GPA yaratish",
                                    value = """
                                        {
                                          "studentId": {"id": "ee2f9738-5992-2b63-a85f-9fa3d98862e5"},
                                          "educationYear": {"code": "2023"},
                                          "level": {"code": "12"},
                                          "gpa": "4.0",
                                          "method": "one_year",
                                          "creditSum": "47.0",
                                          "subjects": 11,
                                          "debtSubjects": 0
                                        }
                                        """
                            )
                    )
            )
            @RequestBody Map<String, Object> requestBody) {

        log.info("[CUBA Entity] POST hemishe_EStudentGpa: {}", requestBody);

        Map<String, Object> result = studentGpaService.create(requestBody);

        log.info("[CUBA Entity] POST hemishe_EStudentGpa: CREATED with ID {}", result.get("id"));
        return ResponseEntity.status(201).body(result);
    }
}
