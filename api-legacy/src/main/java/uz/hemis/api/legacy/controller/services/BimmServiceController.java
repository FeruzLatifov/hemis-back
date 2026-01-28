package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BIMM Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with BIMM (Birlashgan Ijtimoiy Ma'lumotlar Markazi)</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/bimm/*}</p>
 *
 * <p><strong>Response Format:</strong> All endpoints return OLD-HEMIS compatible wrapper:</p>
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... actual data ... }
 * }
 * </pre>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/services/bimm")
@Tag(name = "66.BIMM", description = "BIMM integratsiya xizmatlari - Birlashgan Ijtimoiy Ma'lumotlar Markazi")
@RequiredArgsConstructor
@Slf4j
public class BimmServiceController {

    /**
     * Creates OLD-HEMIS compatible response wrapper
     *
     * @param data the actual response data
     * @return LinkedHashMap with {success, data} structure
     */
    private Map<String, Object> wrapResponse(Map<String, Object> data) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * Check disability status from BIMM
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/disabilityCheck}</p>
     *
     * @param pinfl citizen PINFL
     * @param document document number
     * @return disability information wrapped in {success, data}
     */
    @GetMapping("/disabilityCheck")
    @Operation(
        summary = "Nogironlik holatini tekshirish",
        description = "BIMM tizimidan fuqaroning nogironlik holatini tekshiradi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"document\": \"AB1234567\", \"hasDisability\": false, \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> disabilityCheck(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "12345678901234")
            @RequestParam String pinfl,
            @Parameter(description = "Hujjat raqami", required = false, example = "AB1234567")
            @RequestParam(required = false) String document) {
        log.info("[CUBA Service] bimm/disabilityCheck: pinfl={}, document={}", pinfl, document);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("document", document);
        data.put("hasDisability", false);
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Check poverty register status
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/provertyRegister}</p>
     *
     * @param pinfl citizen PINFL
     * @return poverty register information wrapped in {success, data}
     */
    @GetMapping("/provertyRegister")
    @Operation(
        summary = "Kam ta'minlangan oilalar ro'yxatini tekshirish",
        description = "Fuqaroning kam ta'minlangan oilalar ro'yxatida borligini tekshiradi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"inRegister\": false, \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> provertyRegister(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/provertyRegister: pinfl={}", pinfl);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("inRegister", false);
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get certificate information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/certificate}</p>
     *
     * @param pinfl citizen PINFL
     * @return certificate data wrapped in {success, data}
     */
    @GetMapping("/certificate")
    @Operation(
        summary = "Sertifikat ma'lumotlarini olish",
        description = "BIMM tizimidan fuqaroning sertifikat ma'lumotlarini oladi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"certificates\": [], \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> certificate(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/certificate: pinfl={}", pinfl);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("certificates", List.of());
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get academic degree information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/academicDegree}</p>
     *
     * @param pinfl citizen PINFL
     * @return academic degree data wrapped in {success, data}
     */
    @GetMapping("/academicDegree")
    @Operation(
        summary = "Ilmiy daraja ma'lumotlarini olish",
        description = "BIMM tizimidan fuqaroning ilmiy daraja ma'lumotlarini oladi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"degrees\": [], \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> academicDegree(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/academicDegree: pinfl={}", pinfl);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("degrees", List.of());
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get teacher training information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/bimm/teacherTraining}</p>
     *
     * @param pinfl citizen PINFL
     * @return teacher training data wrapped in {success, data}
     */
    @GetMapping("/teacherTraining")
    @Operation(
        summary = "O'qituvchi malaka oshirish ma'lumotlarini olish",
        description = "BIMM tizimidan o'qituvchining malaka oshirish ma'lumotlarini oladi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"trainings\": [], \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> teacherTraining(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "12345678901234")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] bimm/teacherTraining: pinfl={}", pinfl);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("trainings", List.of());
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }
}
