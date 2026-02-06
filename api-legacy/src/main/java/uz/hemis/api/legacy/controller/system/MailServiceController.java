package uz.hemis.api.legacy.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mail Service Controller - OLD-HEMIS Compatible
 *
 * <p><strong>URL Patterns:</strong></p>
 * <ul>
 *   <li>{@code POST /services/mail/send} - Email yuborish</li>
 *   <li>{@code POST /services/send/verifyCode} - Tasdiqlash kodi yuborish</li>
 * </ul>
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>OLD-HEMIS formatiga mos email va tasdiqlash kodi xizmatlari</li>
 *   <li>Stub implementation - ma'lumotlarni qaytarib yuboradi</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "52.Mail", description = "Email va tasdiqlash kodi xizmatlari")
@RestController
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class MailServiceController {

    /**
     * Send email - OLD-HEMIS format
     *
     * <p><strong>Endpoint:</strong> POST /services/mail/send</p>
     *
     * <p>Stub implementation that echoes back the request data in OLD-HEMIS format.</p>
     *
     * @param request Email request containing id, resetLink, and to fields
     * @return Response with success status and echoed data
     */
    @Operation(
        summary = "Email yuborish (OLD-HEMIS format)",
        description = """
            OLD-HEMIS formatiga mos email yuborish xizmati.

            Bu stub implementation bo'lib, so'rov ma'lumotlarini qaytarib yuboradi.

            **So'rov formati:**
            ```json
            {
                "id": "999999",
                "resetLink": "https://hemis.uz/reset_url",
                "to": "no-reply@hemis.uz"
            }
            ```

            **Javob formati:**
            ```json
            {
                "success": true,
                "id": "999999",
                "reset_link": "https://hemis.uz/reset_url",
                "to": "no-reply@hemis.uz"
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email muvaffaqiyatli yuborildi",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Muvaffaqiyatli javob",
                    value = """
                        {
                            "success": true,
                            "id": "999999",
                            "reset_link": "https://hemis.uz/reset_url",
                            "to": "no-reply@hemis.uz"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    @PostMapping({"/app/rest/v2/services/mail/send", "/services/mail/send"})
    public ResponseEntity<Map<String, Object>> sendMail(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email ma'lumotlari",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Email so'rovi",
                    value = """
                        {
                            "id": "999999",
                            "resetLink": "https://hemis.uz/reset_url",
                            "to": "no-reply@hemis.uz"
                        }
                        """
                )
            )
        )
        @RequestBody Map<String, Object> request
    ) {
        log.info("POST /services/mail/send - to: {}, id: {}",
            request.get("to"), request.get("id"));

        // Use LinkedHashMap to preserve field order (OLD-HEMIS compatibility)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("id", request.get("id"));
        response.put("reset_link", request.get("resetLink"));
        response.put("to", request.get("to"));

        log.debug("Mail send response: {}", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Send verification code - OLD-HEMIS format
     *
     * <p><strong>Endpoint:</strong> POST /services/send/verifyCode</p>
     *
     * <p>Stub implementation that echoes back the request data in OLD-HEMIS format.</p>
     *
     * @param request Verify code request containing id, phone, email, and verify_code fields
     * @return Response with email object containing success status and echoed data
     */
    @Operation(
        summary = "Tasdiqlash kodi yuborish (OLD-HEMIS format)",
        description = """
            OLD-HEMIS formatiga mos tasdiqlash kodi yuborish xizmati.

            Bu stub implementation bo'lib, so'rov ma'lumotlarini qaytarib yuboradi.

            **So'rov formati:**
            ```json
            {
                "id": "999999",
                "phone": "",
                "email": "kanet4u@gmail.com",
                "verify_code": "123456"
            }
            ```

            **Javob formati:**
            ```json
            {
                "email": {
                    "success": true,
                    "verify_code": "123456",
                    "email": "kanet4u@gmail.com"
                }
            }
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tasdiqlash kodi muvaffaqiyatli yuborildi",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Muvaffaqiyatli javob",
                    value = """
                        {
                            "email": {
                                "success": true,
                                "verify_code": "123456",
                                "email": "kanet4u@gmail.com"
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    @PostMapping({"/app/rest/v2/services/send/verifyCode", "/services/send/verifyCode"})
    public ResponseEntity<Map<String, Object>> sendVerifyCode(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Tasdiqlash kodi ma'lumotlari",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    name = "Tasdiqlash kodi so'rovi",
                    value = """
                        {
                            "id": "999999",
                            "phone": "",
                            "email": "kanet4u@gmail.com",
                            "verify_code": "123456"
                        }
                        """
                )
            )
        )
        @RequestBody Map<String, Object> request
    ) {
        log.info("POST /services/send/verifyCode - email: {}, phone: {}, id: {}",
            request.get("email"), request.get("phone"), request.get("id"));

        // Build email response object with LinkedHashMap to preserve field order
        Map<String, Object> emailResponse = new LinkedHashMap<>();
        emailResponse.put("success", true);
        emailResponse.put("verify_code", request.get("verify_code"));
        emailResponse.put("email", request.get("email"));

        // Build main response with LinkedHashMap to preserve field order
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("email", emailResponse);

        log.debug("Verify code response: {}", response);
        return ResponseEntity.ok(response);
    }
}
