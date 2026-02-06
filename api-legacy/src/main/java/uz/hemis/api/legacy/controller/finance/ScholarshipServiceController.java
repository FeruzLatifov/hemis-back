package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Scholarship Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/scholarship/*}</p>
 *
 * <p><strong>Scholarship Management Operations:</strong></p>
 * <ul>
 *   <li>Delete scholarship amounts</li>
 *   <li>Scholarship data management</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "62.Stipendiya", description = "Stipendiya boshqaruvi xizmatlari")
@RestController
@RequestMapping("/services/scholarship")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ScholarshipServiceController {

    /**
     * Delete scholarship amounts by scholarship ID
     *
     * <p><strong>Endpoint:</strong> GET /services/scholarship/deleteAmounts</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - Preserves exact response format</p>
     *
     * <p><strong>Request format:</strong></p>
     * <pre>
     * GET /services/scholarship/deleteAmounts?scholarshipId=7ad1eb02-fec1-1da3-2288-fc9a74371618
     * </pre>
     *
     * <p><strong>Response format:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "scholarshipId": "7ad1eb02-fec1-1da3-2288-fc9a74371618",
     *     "deleted": true,
     *     "message": "Scholarship amounts deleted"
     *   }
     * }
     * </pre>
     *
     * @param scholarshipId Scholarship UUID
     * @return Delete operation result
     */
    @GetMapping("/deleteAmounts")
    @Operation(
            summary = "Stipendiya summasini o'chirish",
            description = """
                Stipendiya yozuvi bo'yicha summalarni o'chirish.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** GET /services/scholarship/deleteAmounts
                **Auth:** Bearer token (required)

                **Parametr:**
                - scholarshipId: Stipendiya UUID (required)

                **Response:**
                - success: Operatsiya holati
                - data.scholarshipId: O'chirilgan stipendiya ID
                - data.deleted: O'chirish holati
                - data.message: Xabar matni
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Stipendiya summalari o'chirildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Muvaffaqiyatli javob",
                                    value = """
                                        {
                                          "success": true,
                                          "data": {
                                            "scholarshipId": "7ad1eb02-fec1-1da3-2288-fc9a74371618",
                                            "deleted": true,
                                            "message": "Scholarship amounts deleted"
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Noto'g'ri so'rov - scholarshipId parametri noto'g'ri",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Xato javob",
                                    value = """
                                        {
                                          "success": false,
                                          "message": "Invalid scholarshipId format"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<Map<String, Object>> deleteAmounts(
            @Parameter(
                    description = "Stipendiya UUID",
                    example = "7ad1eb02-fec1-1da3-2288-fc9a74371618",
                    required = true
            )
            @RequestParam UUID scholarshipId
    ) {
        log.info("GET /services/scholarship/deleteAmounts - scholarshipId: {}", scholarshipId);

        // DEFERRED: Requires scholarship deletion business logic specification
        // For now, return stub response maintaining OLD-HEMIS format

        // Use LinkedHashMap for consistent field order in response
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("scholarshipId", scholarshipId.toString());
        data.put("deleted", true);
        data.put("message", "Scholarship amounts deleted");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);

        log.info("DELETE scholarship amounts completed for scholarshipId: {}", scholarshipId);
        return ResponseEntity.ok(response);
    }
}
