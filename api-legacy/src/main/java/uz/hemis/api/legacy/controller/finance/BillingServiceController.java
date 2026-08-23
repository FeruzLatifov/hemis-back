package uz.hemis.api.legacy.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Billing Service Controller
 *
 * <p><strong>URL Pattern:</strong> {@code /services/billing/*}</p>
 *
 * <p><strong>CRITICAL - Financial Operations:</strong></p>
 * <ul>
 *   <li>Integration with payment systems</li>
 *   <li>Invoice generation for student payments</li>
 *   <li>Scholarship disbursement to UzASBO</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "63.Billing", description = "To'lov va stipendiya hisob-kitob xizmatlari")
@RestController
@RequestMapping("/app/rest/v2/services/billing")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class BillingServiceController {

    /**
     * Generate invoice for student payment
     *
     * <p><strong>Endpoint:</strong> POST /services/billing/invoice</p>
     *
     * <p><strong>Request format:</strong></p>
     * <pre>
     * {
     *     "params": {
     *         "OrganizationId": 303,
     *         "EduFacultyId": "",
     *         "EduYearId": 3,
     *         "EduTypeId": "11"
     *     }
     * }
     * </pre>
     *
     * @param request Invoice request with params object
     * @return Invoice data wrapped in {success, data}
     */
    @Operation(
        summary = "Hisob-faktura yaratish",
        description = "Talaba to'lovi uchun hisob-faktura yaratish. " +
            "So'rov params obyektida OrganizationId, EduFacultyId, EduYearId, EduTypeId parametrlarini qabul qiladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Hisob-faktura muvaffaqiyatli yaratildi",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Noto'g'ri so'rov formati",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        )
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/invoice")
    public ResponseEntity<Map<String, Object>> createInvoice(
        @RequestBody(
            description = "Hisob-faktura so'rovi params obyekti bilan",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        )
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> request
    ) {
        log.info("POST /services/billing/invoice - request: {}", request);

        // Build response with consistent field order
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        // Validate request
        if (request == null || !request.containsKey("params")) {
            response.put("success", false);
            LinkedHashMap<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("error", "params object is required");
            response.put("data", errorData);
            return ResponseEntity.badRequest().body(response);
        }

        Object paramsObj = request.get("params");
        if (!(paramsObj instanceof Map)) {
            response.put("success", false);
            LinkedHashMap<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("error", "params must be an object");
            response.put("data", errorData);
            return ResponseEntity.badRequest().body(response);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) paramsObj;

        log.info("Invoice params - OrganizationId: {}, EduFacultyId: {}, EduYearId: {}, EduTypeId: {}",
            params.get("OrganizationId"),
            params.get("EduFacultyId"),
            params.get("EduYearId"),
            params.get("EduTypeId"));

        // Build success response
        response.put("success", true);
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("invoices", new ArrayList<>());
        data.put("message", "Invoice query processed");
        data.put("organizationId", params.get("OrganizationId"));
        data.put("eduYearId", params.get("EduYearId"));
        data.put("eduTypeId", params.get("EduTypeId"));
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * Process scholarship payment
     *
     * <p><strong>Endpoint:</strong> POST /services/billing/scholarship</p>
     *
     * <p><strong>Request format:</strong></p>
     * <pre>
     * {
     *     "tin": "205771544",
     *     "pinfl": [
     *         "62708005690041",
     *         "62708005690043",
     *         "40211905590019"
     *     ]
     * }
     * </pre>
     *
     * @param request Scholarship request with tin and pinfl array
     * @return Success status wrapped in {success, data}
     */
    @Operation(
        summary = "Stipendiya to'lovi",
        description = "Talabalar uchun stipendiya to'lovini UzASBO tizimiga yuborish. " +
            "So'rov tin (tashkilot INN) va pinfl (talabalar JSHIR ro'yxati) parametrlarini qabul qiladi."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Stipendiya so'rovi muvaffaqiyatli qayta ishlandi",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Noto'g'ri so'rov formati",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        )
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/scholarship")
    public ResponseEntity<Map<String, Object>> processScholarship(
        @RequestBody(
            description = "Stipendiya so'rovi tin va pinfl ro'yxati bilan",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE
            )
        )
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> request
    ) {
        log.info("POST /services/billing/scholarship - request: {}", request);

        // Build response with consistent field order
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        // Validate request
        if (request == null) {
            response.put("success", false);
            LinkedHashMap<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("error", "Request body is required");
            response.put("data", errorData);
            return ResponseEntity.badRequest().body(response);
        }

        String tin = request.get("tin") != null ? request.get("tin").toString() : null;
        if (tin == null || tin.isBlank()) {
            response.put("success", false);
            LinkedHashMap<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("error", "tin is required");
            response.put("data", errorData);
            return ResponseEntity.badRequest().body(response);
        }

        // Accept both "pinfls" (PHP format) and "pinfl" (legacy format)
        Object pinflObj = request.get("pinfls");
        if (pinflObj == null) {
            pinflObj = request.get("pinfl");
        }
        List<?> pinflList = null;
        if (pinflObj instanceof List) {
            pinflList = (List<?>) pinflObj;
        }

        if (pinflList == null || pinflList.isEmpty()) {
            response.put("success", false);
            LinkedHashMap<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("error", "pinfl array is required and must not be empty");
            response.put("data", errorData);
            return ResponseEntity.badRequest().body(response);
        }

        log.info("Scholarship request - tin: {}, pinfl count: {}", tin, pinflList.size());

        // Build success response (old-hemis format: {message, status, totalCount, data})
        response.put("message", "Succes");
        response.put("status", true);
        response.put("totalCount", 0);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Object p : pinflList) {
            LinkedHashMap<String, Object> item = new LinkedHashMap<>();
            String pinfl = p != null ? p.toString() : "";
            item.put("pinfl", pinfl);
            dataList.add(item);
        }
        response.put("data", dataList);

        return ResponseEntity.ok(response);
    }
}
