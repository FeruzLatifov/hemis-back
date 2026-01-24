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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Student Practice Entity Controller (CUBA Pattern)
 * Tag 69: Amaliyot (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EStudentPractice
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_EStudentPractice
 * - Response format: CUBA Map structure with _entityName, _instanceName
 *
 * Note: This is a STUB implementation - the actual StudentPractice entity
 * does not exist yet in the database.
 */
@Tag(name = "69.Amaliyot", description = "Talaba amaliyot ma'lumotlari")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudentPractice")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentPracticeEntityController {

    private static final String ENTITY_NAME = "hemishe_EStudentPractice";

    /**
     * Create student practice record
     *
     * <p><strong>Endpoint:</strong> POST /app/rest/v2/entities/hemishe_EStudentPractice</p>
     *
     * @param practiceData Practice data from request body
     * @return Created practice record with generated ID
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Amaliyot yaratish",
        description = "Talaba amaliyot yozuvini yaratish. STUB implementation - haqiqiy entity mavjud emas."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Amaliyot muvaffaqiyatli yaratildi",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                        {
                            "_entityName": "hemishe_EStudentPractice",
                            "_instanceName": "Practice-2025-01-15",
                            "id": "generated-uuid"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> createPractice(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Amaliyot ma'lumotlari",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                        {
                            "university": { "code": "999" },
                            "student": { "id": "uuid" },
                            "practiceType": { "code": "1" },
                            "startDate": "2025-01-15",
                            "endDate": "2025-02-15",
                            "organization": "Test Tashkilot",
                            "active": true
                        }
                        """
                )
            )
        )
        @RequestBody Map<String, Object> practiceData
    ) {
        log.info("POST /app/rest/v2/entities/hemishe_EStudentPractice - data: {}", practiceData);

        // Generate response in OLD-HEMIS format
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", "Practice-" + practiceData.getOrDefault("startDate", "unknown"));
        response.put("id", UUID.randomUUID().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Get student practice by ID
     *
     * @param id Practice ID
     * @return Practice data or 404
     */
    @GetMapping("/{id}")
    @Operation(summary = "Amaliyot olish", description = "ID bo'yicha amaliyot ma'lumotlarini olish")
    public ResponseEntity<Map<String, Object>> getPractice(
        @Parameter(description = "Amaliyot ID") @PathVariable UUID id
    ) {
        log.info("GET /app/rest/v2/entities/hemishe_EStudentPractice/{}", id);

        // Stub - return not found since entity doesn't exist
        return ResponseEntity.notFound().build();
    }

    /**
     * List all practices with pagination
     *
     * @param limit Max results (default 50)
     * @param offset Offset for pagination (default 0)
     * @return Empty list (stub)
     */
    @GetMapping
    @Operation(summary = "Amaliyotlar ro'yxati", description = "Barcha amaliyotlar ro'yxatini olish (pagination bilan)")
    public ResponseEntity<List<Map<String, Object>>> listPractices(
        @Parameter(description = "Max results") @RequestParam(defaultValue = "50") int limit,
        @Parameter(description = "Offset") @RequestParam(defaultValue = "0") int offset
    ) {
        log.info("GET /app/rest/v2/entities/hemishe_EStudentPractice - limit: {}, offset: {}", limit, offset);

        // Return empty list since entity doesn't exist
        return ResponseEntity.ok(Collections.emptyList());
    }
}
