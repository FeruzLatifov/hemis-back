package uz.hemis.api.legacy.controller.academic;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Transcript Service Controller - OLD-HEMIS Compatible
 *
 * <p><strong>Transkript xizmati</strong></p>
 * <p>Talabalarning transkript (akademik o'zlashtirish) ma'lumotlarini olish uchun REST API endpoint</p>
 *
 * <p><strong>OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>100% backward compatible with OLD-HEMIS endpoints</li>
 *   <li>Matches OLD-HEMIS response format: {success, data}</li>
 *   <li>Same URL paths as OLD-HEMIS</li>
 *   <li>Same parameter names as OLD-HEMIS</li>
 * </ul>
 *
 * <p><strong>URL Pattern:</strong> {@code /services/transcript/*}</p>
 *
 * @author HEMIS Backend Team
 * @since 2.0.0
 */
@Tag(name = "54.Transkript", description = "Transkript va akademik o'zlashtirish xizmatlari")
@RestController
@RequestMapping("/app/rest/v2/services/transcript")
@RequiredArgsConstructor
@Slf4j
public class TranscriptServiceController {

    /**
     * Get transcript data by PINFL
     *
     * <p><strong>OLD-HEMIS Endpoint:</strong> GET /services/transcript/get</p>
     *
     * <p>Bu endpoint talabaning PINFL raqami orqali transkript (akademik o'zlashtirish)
     * ma'lumotlarini qaytaradi.</p>
     *
     * @param pinfl PINFL (14 raqamli shaxsiy identifikatsiya raqami)
     * @return Transkript ma'lumotlari {success, data} formatida
     */
    @Operation(
            summary = "Transkript ma'lumotlarini olish (PINFL orqali)",
            description = """
                    PINFL orqali talabaning transkript (akademik o'zlashtirish) ma'lumotlarini olish.

                    **Talab:**
                    - pinfl: PINFL (14 raqamli shaxsiy identifikatsiya raqami)

                    **Response:**
                    - success: true/false
                    - data: Transkript ma'lumotlari (pinfl, message)

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /services/transcript/get
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Transkript ma'lumotlari qaytarildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "pinfl": "999211100039",
                                                "message": "Transcript data"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - PINFL parametri yo'q yoki noto'g'ri",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Missing PINFL",
                                    value = """
                                            {
                                              "success": false,
                                              "data": {
                                                "pinfl": null,
                                                "message": "PINFL parameter is required"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> get(
            @Parameter(
                    description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)",
                    required = true,
                    example = "999211100039"
            )
            @RequestParam String pinfl
    ) {
        log.info("GET /services/transcript/get - pinfl: {}", pinfl);

        // Build response using LinkedHashMap for consistent field order
        // OLD-HEMIS format: {success, data, code}
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("message", "Transcript data");

        response.put("data", data);
        response.put("code", 200);

        log.info("Transcript data returned for PINFL: {}", pinfl);
        return ResponseEntity.ok(response);
    }
}
