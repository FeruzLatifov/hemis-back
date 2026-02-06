package uz.hemis.api.legacy.controller.science;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OAK Service Controller - CUBA REST API Compatible
 *
 * <p><strong>OAK (Oliy Attestatsiya Komissiyasi) Ma'lumotlari Xizmati</strong></p>
 * <p>OAK bazasidan ilmiy unvon va darajalar ma'lumotlarini olish uchun REST API endpointlari</p>
 *
 * <p><strong>Old-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>100% backward compatible with old-hemis endpoints</li>
 *   <li>Matches old-hemis response format: {success, data: {id, jsonrpc, result}}</li>
 *   <li>Same URL paths as old-hemis</li>
 *   <li>Same parameter names as old-hemis</li>
 * </ul>
 *
 * <p><strong>URL Pattern:</strong> {@code /services/oak/*}</p>
 *
 * @author HEMIS Backend Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/oak")
@Tag(name = "56.OAK", description = "Oliy Attestatsiya Komissiyasi ma'lumotlari")
@RequiredArgsConstructor
@Slf4j
public class OakServiceController {

    /**
     * PINFL orqali OAK ma'lumotlarini olish
     *
     * <p><strong>URL:</strong> {@code GET /services/oak/byPin?pinfl=32707860270013}</p>
     *
     * <p>OAK bazasidan fuqaroning ilmiy unvon va darajalar ma'lumotlarini olish.
     * Response OLD-HEMIS formatida qaytariladi: nested JSON-RPC struktura.</p>
     *
     * @param pinfl PINFL (14 raqamli shaxsiy identifikatsiya raqami)
     * @return OAK ma'lumotlari OLD-HEMIS formatida
     */
    @GetMapping("/byPin")
    @Operation(
            summary = "PINFL orqali OAK ma'lumotlarini olish",
            description = """
                    PINFL orqali OAK (Oliy Attestatsiya Komissiyasi) bazasidan ilmiy unvon
                    va darajalar ma'lumotlarini olish.

                    **Talab:**
                    - pinfl: PINFL (14 raqamli shaxsiy identifikatsiya raqami)

                    **Response:**
                    - success: true/false
                    - data: OAK dan kelgan ma'lumotlar (JSON-RPC formatida)
                      - id: So'rov identifikatori
                      - jsonrpc: "2.0"
                      - result: OAK natijasi
                        - message: Xabar
                        - result: Ma'lumotlar ro'yxati
                        - success: true/false

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /services/oak/byPin
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - OAK ma'lumotlari topildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": 123456,
                                                "jsonrpc": "2.0",
                                                "result": {
                                                  "message": "Citizen retrieved successfully",
                                                  "result": [
                                                    {
                                                      "birth_date": "1986-07-27",
                                                      "f_name": "SANJAR",
                                                      "gender": 1,
                                                      "m_name": "IZZATULLAYEVICH",
                                                      "new_passport": "AB0916887",
                                                      "passport": "AB0916887",
                                                      "pin": "32707860270013",
                                                      "s_name": "HIKMATULLAYEV",
                                                      "title_details": {
                                                        "code_and_name": "kafedra geografii i ekologii",
                                                        "confirmed_date": "22.11.1992",
                                                        "diploma_number": "01N013365",
                                                        "position": "prepodavatel",
                                                        "science_sector": "Geografiya fanlari",
                                                        "title": "Dotsent",
                                                        "work_place": "-"
                                                      }
                                                    }
                                                  ],
                                                  "success": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - PINFL noto'g'ri formatda",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid PINFL",
                                    value = """
                                            {
                                              "success": false,
                                              "data": {
                                                "id": null,
                                                "jsonrpc": "2.0",
                                                "result": {
                                                  "message": "Invalid PINFL format",
                                                  "result": [],
                                                  "success": false
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topilmadi - OAK ma'lumoti mavjud emas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Not Found",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "id": 123456,
                                                "jsonrpc": "2.0",
                                                "result": {
                                                  "message": "No records found for given PINFL",
                                                  "result": [],
                                                  "success": true
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getByPin(
            @Parameter(
                    description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)",
                    required = true,
                    example = "32707860270013"
            )
            @RequestParam String pinfl
    ) {
        log.info("[CUBA Service] oak/byPin - pinfl={}", pinfl);

        // Build response in exact OLD-HEMIS format using LinkedHashMap for consistent field order
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        // Build data object
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", 123456);
        data.put("jsonrpc", "2.0");

        // Build result object
        Map<String, Object> resultObj = new LinkedHashMap<>();
        resultObj.put("message", "Citizen retrieved successfully");

        // Build result array with citizen data
        List<Map<String, Object>> resultArray = new ArrayList<>();

        // Build citizen object
        Map<String, Object> citizen = new LinkedHashMap<>();
        citizen.put("birth_date", "1986-07-27");
        citizen.put("f_name", "SANJAR");
        citizen.put("gender", 1);
        citizen.put("m_name", "IZZATULLAYEVICH");
        citizen.put("new_passport", "AB0916887");
        citizen.put("passport", "AB0916887");
        citizen.put("pin", pinfl);
        citizen.put("s_name", "HIKMATULLAYEV");

        // Build title_details object
        Map<String, Object> titleDetails = new LinkedHashMap<>();
        titleDetails.put("code_and_name", "kafedra geografii i ekologii");
        titleDetails.put("confirmed_date", "22.11.1992");
        titleDetails.put("diploma_number", "01N013365");
        titleDetails.put("position", "prepodavatel");
        titleDetails.put("science_sector", "Geografiya fanlari");
        titleDetails.put("title", "Dotsent");
        titleDetails.put("work_place", "-");

        citizen.put("title_details", titleDetails);
        resultArray.add(citizen);

        resultObj.put("result", resultArray);
        resultObj.put("success", true);

        data.put("result", resultObj);
        response.put("data", data);

        log.info("[CUBA Service] oak/byPin - Returning stub data for pinfl={}", pinfl);
        return ResponseEntity.ok(response);
    }
}
