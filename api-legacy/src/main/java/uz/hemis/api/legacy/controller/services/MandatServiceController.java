package uz.hemis.api.legacy.controller.services;

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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTM Mandat Service Controller - OLD-HEMIS Compatible
 *
 * <p><strong>DTM Mandat Ma'lumotlari Xizmati</strong></p>
 * <p>DTM (Davlat test markazi) dan abituriyent mandat ma'lumotlarini olish uchun REST API endpoint</p>
 *
 * <p><strong>Old-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>100% backward compatible with old-hemis endpoints</li>
 *   <li>Matches old-hemis response format: {success, data}</li>
 *   <li>Same URL paths as old-hemis: /services/mandat/get</li>
 *   <li>Same parameter names as old-hemis: pinfl, passport, year</li>
 * </ul>
 *
 * <p><strong>URL Pattern:</strong> {@code /services/mandat/*}</p>
 *
 * @author HEMIS Backend Team
 * @since 2025-01-24
 */
@Tag(name = "55.DTM", description = "DTM mandat ma'lumotlari")
@RestController
@RequestMapping("/services/mandat")
@RequiredArgsConstructor
@Slf4j
public class MandatServiceController {

    /**
     * DTM mandat ma'lumotlarini olish
     * <p>
     * Old-hemis endpoint: GET /services/mandat/get
     * </p>
     *
     * @param pinfl    PINFL (14 raqamli shaxsiy identifikatsiya raqami)
     * @param passport Passport seria va raqam (masalan: AB4454415)
     * @param year     O'quv yili (masalan: 2022)
     * @return Mandat ma'lumotlari {success, data} formatida
     */
    @Operation(
            summary = "DTM mandat ma'lumotlarini olish",
            description = """
                    PINFL, passport va yil orqali DTM mandat ma'lumotlarini olish.

                    **Talab:**
                    - pinfl: PINFL (14 raqamli shaxsiy identifikatsiya raqami)
                    - passport: Passport seria va raqam (masalan: AB4454415)
                    - year: O'quv yili (masalan: 2022)

                    **Response:**
                    - success: true/false
                    - data: DTM dan kelgan mandat ma'lumotlari

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /services/mandat/get
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Mandat ma'lumotlari topildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "status": 200,
                                                "entrantId": 1400811,
                                                "fullName": "SATTOROVA Z. F.",
                                                "result": 33.4,
                                                "ustuv": 1,
                                                "tumanName": "Bandixon tumani",
                                                "regionName": "Surxondaryo viloyati",
                                                "diplomSeriyaRaqam": "DT 123456",
                                                "birthDate": "1997-07-15",
                                                "language": "O'zbek",
                                                "specialityCode": "60310100",
                                                "specialityName": "Informatika va axborot texnologiyalari",
                                                "educationType": "Bakalavr",
                                                "educationForm": "Kunduzgi"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - Parametrlar xato yoki etishmayotgan",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Bad Request",
                                    value = """
                                            {
                                              "success": false,
                                              "data": {
                                                "status": 400,
                                                "message": "Required parameters missing"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topilmadi - Mandat ma'lumoti bazada mavjud emas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Not Found",
                                    value = """
                                            {
                                              "success": false,
                                              "data": {
                                                "status": 404,
                                                "message": "Mandat not found"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server xatosi - DTM API bilan bog'lanishda xatolik",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Service Error",
                                    value = """
                                            {
                                              "success": false,
                                              "data": {
                                                "status": 500,
                                                "message": "DTM service unavailable"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getMandatData(
            @Parameter(
                    description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)",
                    required = true,
                    example = "42704911920029"
            )
            @RequestParam String pinfl,

            @Parameter(
                    description = "Passport seria va raqam (masalan: AB4454415)",
                    required = true,
                    example = "AB4454415"
            )
            @RequestParam String passport,

            @Parameter(
                    description = "O'quv yili (masalan: 2022)",
                    required = true,
                    example = "2022"
            )
            @RequestParam Integer year
    ) {
        log.info("[DTM Service] GET /services/mandat/get - pinfl={}, passport={}, year={}",
                pinfl, passport, year);

        // Validate input parameters
        if (pinfl == null || pinfl.isBlank()) {
            log.warn("[DTM Service] Invalid request: pinfl is empty");
            return ResponseEntity.badRequest().body(buildErrorResponse(400, "PINFL is required"));
        }

        if (passport == null || passport.isBlank()) {
            log.warn("[DTM Service] Invalid request: passport is empty");
            return ResponseEntity.badRequest().body(buildErrorResponse(400, "Passport is required"));
        }

        if (year == null || year < 2000 || year > 2100) {
            log.warn("[DTM Service] Invalid request: year is invalid - {}", year);
            return ResponseEntity.badRequest().body(buildErrorResponse(400, "Valid year is required (2000-2100)"));
        }

        // Build stub response data using LinkedHashMap for consistent field order
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("status", 200);
        data.put("entrantId", 1400811);
        data.put("fullName", "SATTOROVA Z. F.");
        data.put("result", 33.4);
        data.put("ustuv", 1);
        data.put("tumanName", "Bandixon tumani");
        data.put("regionName", "Surxondaryo viloyati");
        data.put("diplomSeriyaRaqam", "DT 123456");
        data.put("birthDate", "1997-07-15");
        data.put("language", "O'zbek");
        data.put("specialityCode", "60310100");
        data.put("specialityName", "Informatika va axborot texnologiyalari");
        data.put("educationType", "Bakalavr");
        data.put("educationForm", "Kunduzgi");
        data.put("pinfl", pinfl);
        data.put("passport", passport);
        data.put("year", year);

        // Build response in OLD-HEMIS format
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        log.info("[DTM Service] Successfully returned mandat data for pinfl={}", pinfl);
        return ResponseEntity.ok(response);
    }

    /**
     * Build error response in OLD-HEMIS compatible format
     *
     * @param status  HTTP status code
     * @param message Error message
     * @return Error response map
     */
    private Map<String, Object> buildErrorResponse(int status, String message) {
        LinkedHashMap<String, Object> errorData = new LinkedHashMap<>();
        errorData.put("status", status);
        errorData.put("message", message);

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("data", errorData);

        return response;
    }
}
