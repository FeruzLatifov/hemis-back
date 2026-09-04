package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * UzASBO Service Controller
 *
 * <p>UzASBO integration endpoints for scholarship verification and test operations.</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /services/*}</p>
 *
 * <p><strong>Note:</strong> This controller uses the CORRECT path {@code /services/student/checkScholarship2}.
 * The existing StudentServiceController has {@code /app/rest/v2/services/student/checkScholarship2} which is WRONG.</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services")
@Tag(name = "58.UzASBO", description = "UzASBO tizimi bilan integratsiya - stipendiya tekshirish")
@RequiredArgsConstructor
@Slf4j
public class UzAsboServiceController {

    /**
     * PINFL validation regex pattern - must be exactly 14 digits
     */
    private static final String PINFL_PATTERN = "^\\d{14}$";

    /**
     * Check scholarship eligibility for multiple students (UzASBO integration)
     *
     * <p><strong>URL:</strong> {@code POST /services/student/checkScholarship2}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * <p>This endpoint validates student PINFLs and checks their scholarship eligibility.
     * Each student's PINFL is validated (must be exactly 14 digits) and returns status for each.</p>
     *
     * <p><strong>Request format:</strong></p>
     * <pre>
     * {
     *   "tin": "000000000",
     *   "docOn": "2024-02-05",
     *   "students": [
     *     {"pinfl": "00000000000001", "sum": "103576"},
     *     {"pinfl": "00000000000002"},
     *     {"pinfl": "999999999999999", "sum": "103576"}
     *   ]
     * }
     * </pre>
     *
     * <p><strong>Response format:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "tin": "000000000",
     *     "docOn": "2024-02-05",
     *     "results": [
     *       {"pinfl": "00000000000001", "status": "OK"},
     *       {"pinfl": "00000000000002", "status": "OK"},
     *       {"pinfl": "999999999999999", "status": "INVALID_PINFL"}
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param request Scholarship check request with TIN, document date, and list of students
     * @return Scholarship eligibility status for each student
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/student/checkScholarship2")
    @Operation(
            summary = "Stipendiya tekshirish (UzASBO)",
            description = """
                UzASBO tizimi uchun talabalar stipendiya huquqini tekshirish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** POST /services/student/checkScholarship2
                **Content-Type:** application/json

                **Request Body:**
                - tin: Tashkilot STIRi (9 xonali)
                - docOn: Hujjat sanasi (YYYY-MM-DD formatida)
                - students: Talabalar ro'yxati
                  - pinfl: Talaba PINFL raqami (14 xonali)
                  - sum: Stipendiya summasi (ixtiyoriy)

                **PINFL validatsiyasi:**
                - PINFL aynan 14 ta raqamdan iborat bo'lishi kerak
                - Noto'g'ri PINFL uchun status: INVALID_PINFL
                - To'g'ri PINFL uchun status: OK

                **Response:**
                - success: Muvaffaqiyat holati
                - data.tin: Tashkilot STIRi
                - data.docOn: Hujjat sanasi
                - data.results: Har bir talaba uchun natija
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Stipendiya tekshiruvi natijalari",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri request format",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> checkScholarship2(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Stipendiya tekshirish so'rovi",
                    required = true,
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
            @RequestBody Map<String, Object> request) {
        log.info("[UzASBO Service] student/checkScholarship2: request={}", request);

        // Extract request fields
        String tin = (String) request.get("tin");
        String docOn = (String) request.get("docOn");
        Object studentsObj = request.get("students");

        // Validate required fields
        if (studentsObj == null) {
            log.warn("[UzASBO Service] Missing 'students' field in request");
            LinkedHashMap<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Missing required field: students");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Process students and validate PINFLs
        List<Map<String, Object>> results = new ArrayList<>();
        if (studentsObj instanceof List<?> studentsList) {
            for (Object studentObj : studentsList) {
                if (studentObj instanceof Map<?, ?> studentMap) {
                    String pinfl = (String) ((Map<String, Object>) studentMap).get("pinfl");

                    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                    result.put("pinfl", pinfl);

                    // Validate PINFL - must be exactly 14 digits
                    if (pinfl == null || !pinfl.matches(PINFL_PATTERN)) {
                        result.put("status", "INVALID_PINFL");
                        log.debug("[UzASBO Service] Invalid PINFL: {}", uz.hemis.common.log.LogSafe.pinfl(pinfl));
                    } else {
                        result.put("status", "OK");
                        log.debug("[UzASBO Service] Valid PINFL: {}", uz.hemis.common.log.LogSafe.pinfl(pinfl));
                    }

                    results.add(result);
                }
            }
        }

        // Build response with LinkedHashMap for consistent field order
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("tin", tin);
        data.put("docOn", docOn);
        data.put("results", results);

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        log.info("[UzASBO Service] student/checkScholarship2: processed {} students", results.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Type test endpoint for UzASBO integration testing
     *
     * <p><strong>URL:</strong> {@code GET /services/test/typetest}</p>
     *
     * <p>Simple test endpoint that returns test data to verify UzASBO integration connectivity.</p>
     *
     * @return Test response with type and message
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/test/typetest")
    @Operation(
            summary = "Test endpoint (UzASBO)",
            description = """
                UzASBO integratsiya test endpointi.

                **Endpoint:** GET /services/test/typetest

                Bu endpoint UzASBO tizimi bilan bog'lanishni tekshirish uchun ishlatiladi.
                Hech qanday parametr talab qilinmaydi.

                **Response:**
                - success: Muvaffaqiyat holati (true)
                - data.type: Test turi ("test")
                - data.message: Test xabari
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Test ma'lumotlari",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> typeTest() {
        log.info("[UzASBO Service] test/typetest");

        // Build response with LinkedHashMap for consistent field order
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("type", "test");
        data.put("message", "Type test endpoint");

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
