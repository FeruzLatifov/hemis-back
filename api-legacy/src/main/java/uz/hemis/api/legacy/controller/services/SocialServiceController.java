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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Social Service Controller - CUBA REST API Compatible
 *
 * <p>Integration with Social Protection services</p>
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/social/*}</p>
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
@RequestMapping("/services/social")
@Tag(name = "61.Ijtimoiy himoya", description = "Ijtimoiy himoya xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class SocialServiceController {

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
     * Check single register status
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/singleRegister}</p>
     *
     * @param pinfl citizen PINFL
     * @return single register information wrapped in {success, data}
     */
    @GetMapping("/singleRegister")
    @Operation(
        summary = "Yagona reestr tekshirish",
        description = "Fuqaroning yagona reestrda ro'yxatdan o'tganligini tekshiradi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"registered\": false, \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> singleRegister(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "42601793500108")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] social/singleRegister: pinfl={}", pinfl);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("registered", false);
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get full daftar information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/daftarFull}</p>
     *
     * @param pinfl citizen PINFL
     * @return full daftar data wrapped in {success, data}
     */
    @GetMapping("/daftarFull")
    @Operation(
        summary = "To'liq daftar ma'lumotlari",
        description = "Fuqaroning to'liq daftar ma'lumotlarini oladi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"daftar\": {}, \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> daftarFull(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "42601793500108")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] social/daftarFull: pinfl={}", pinfl);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("daftar", new LinkedHashMap<>());
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get short daftar information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/daftarShort}</p>
     *
     * @param pinfl citizen PINFL
     * @return short daftar data wrapped in {success, data}
     */
    @GetMapping("/daftarShort")
    @Operation(
        summary = "Qisqacha daftar ma'lumotlari",
        description = "Fuqaroning qisqacha daftar ma'lumotlarini oladi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"12345678901234\", \"daftar\": {}, \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> daftarShort(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "42601793500108")
            @RequestParam String pinfl) {
        log.info("[CUBA Service] social/daftarShort: pinfl={}", pinfl);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("daftar", new LinkedHashMap<>());
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get women support information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/women?pinfl=42601793500108&sn=KA0773072}</p>
     *
     * @param pinfl citizen PINFL
     * @param sn serial number (e.g., KA0773072)
     * @return women support data wrapped in {success, data}
     */
    @GetMapping("/women")
    @Operation(
        summary = "Ayollar qo'llab-quvvatlash ma'lumotlari",
        description = "Ayollar qo'llab-quvvatlash dasturi ma'lumotlarini oladi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"42601793500108\", \"sn\": \"KA0773072\", \"support\": [], \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> women(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "42601793500108")
            @RequestParam String pinfl,
            @Parameter(description = "Hujjat seriya va raqami (masalan: KA0773072)", required = false, example = "KA0773072")
            @RequestParam(required = false) String sn) {
        log.info("[CUBA Service] social/women: pinfl={}, sn={}", pinfl, sn);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("sn", sn);
        data.put("support", java.util.List.of());
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }

    /**
     * Get youth support information
     *
     * <p><strong>URL:</strong> {@code GET /app/rest/v2/services/social/young?pinfl=42601793500108&seria=KA&number=0773072}</p>
     *
     * @param pinfl citizen PINFL
     * @param seria document series (e.g., KA)
     * @param number document number (e.g., 0773072)
     * @return youth support data wrapped in {success, data}
     */
    @GetMapping("/young")
    @Operation(
        summary = "Yoshlar qo'llab-quvvatlash ma'lumotlari",
        description = "Yoshlar qo'llab-quvvatlash dasturi ma'lumotlarini oladi. Javob {success, data} formatida qaytariladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Muvaffaqiyatli javob",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"data\": {\"pinfl\": \"42601793500108\", \"seria\": \"KA\", \"number\": \"0773072\", \"support\": [], \"message\": \"Stub implementation\"}}"
                )
            )
        )
    })
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> young(
            @Parameter(description = "Fuqaro PINFL raqami", required = true, example = "42601793500108")
            @RequestParam String pinfl,
            @Parameter(description = "Hujjat seriyasi (masalan: KA)", required = false, example = "KA")
            @RequestParam(required = false) String seria,
            @Parameter(description = "Hujjat raqami (masalan: 0773072)", required = false, example = "0773072")
            @RequestParam(required = false) String number) {
        log.info("[CUBA Service] social/young: pinfl={}, seria={}, number={}", pinfl, seria, number);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("seria", seria);
        data.put("number", number);
        data.put("support", java.util.List.of());
        data.put("message", "Stub implementation - parameters returned");

        return ResponseEntity.ok(wrapResponse(data));
    }
}
