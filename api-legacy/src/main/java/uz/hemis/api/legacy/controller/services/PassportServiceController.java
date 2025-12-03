package uz.hemis.api.legacy.controller.services;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uz.hemis.service.CaptchaService;
import uz.hemis.service.integration.GuvdTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/**
 * Passport Service Controller - GUVD Integration
 *
 * <p><strong>GUVD Passport Ma'lumotlari Xizmati</strong></p>
 * <p>GUVD bazasidan fuqarolarning passport ma'lumotlarini olish uchun REST API endpointlari</p>
 *
 * <p><strong>Old-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>100% backward compatible with old-hemis endpoints</li>
 *   <li>Matches old-hemis response format: {success, data, address}</li>
 *   <li>Same URL paths as old-hemis</li>
 *   <li>Same parameter names as old-hemis</li>
 * </ul>
 *
 * <p><strong>Security:</strong></p>
 * <ul>
 *   <li>All endpoints require authentication (Bearer token)</li>
 *   <li>Captcha validation required for all endpoints</li>
 *   <li>Integration with GUVD e-gov API via apimgw.egov.uz</li>
 * </ul>
 *
 * @author HEMIS Backend Team
 * @since 2025-11-21
 */
@Tag(
        name = "03.Passport ma'lumotlari",
        description = "GUVD passport ma'lumotlarini olish va tekshirish xizmatlari. " +
                "PINFL, seria/raqam va tug'ilgan sana orqali fuqarolarning passport " +
                "ma'lumotlarini GUVD e-gov API orqali olish imkonini beradi."
)
@RestController
@RequestMapping("/app/rest/v2/services/passport-data")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
@SuppressWarnings("unchecked")
public class PassportServiceController {

    private final CaptchaService captchaService;
    private final GuvdTokenService guvdTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // GUVD API endpoints (from .env)
    @Value("${hemis.integration.guvd.passport-api.url:https://apimgw.egov.uz:8243/gcp/docrest/v1}")
    private String guvdPassportApiUrl;

    @Value("${hemis.integration.guvd.address-api.url:https://apimgw.egov.uz:8243/mvd/services/address/info/pin/v1}")
    private String guvdAddressApiUrl;

    /**
     * PINFL va seria/raqam bilan passport ma'lumotini olish
     * <p>
     * Old-hemis endpoint: GET /app/rest/v2/services/passport-data/getDataBySN
     * </p>
     *
     * @param pinfl        PINFL (14 raqamli)
     * @param seriaNumber  Passport seria va raqam (masalan: AB1234567)
     * @param captchaId    Captcha identifikatori
     * @param captchaValue Foydalanuvchi tomonidan kiritilgan captcha qiymati
     * @return Passport ma'lumotlari va manzil
     */
    @Operation(
            summary = "Passport ma'lumotlarni olish (Pinfl va Seria nomer bilan)",
            description = """
                    PINFL va passport seria-raqam orqali GUVD bazasidan passport ma'lumotlarini olish.

                    **Talab:**
                    - PINFL: 14 raqamli shaxsiy identifikatsiya raqami
                    - seriaNumber: Passport seria va raqam (masalan: AB1234567)
                    - captchaId: Captcha identifikatori (GET /services/captcha/getNumericCaptcha)
                    - captchaValue: Foydalanuvchi tomonidan kiritilgan captcha qiymati

                    **Response:**
                    - success: true/false
                    - data: GUVD dan kelgan passport ma'lumotlari
                    - address: Ro'yxatga olingan manzil ma'lumotlari

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /app/rest/v2/services/passport-data/getDataBySN
                    **Auth:** Bearer token (required)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Passport ma'lumotlari topildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "status": 1,
                                                "error": null,
                                                "data": [
                                                  {
                                                    "current_pinpp": "12345678901234",
                                                    "sur_name_latin": "ABDULLAYEV",
                                                    "name_latin": "AKMAL",
                                                    "patronym_name_latin": "AHMADOVICH",
                                                    "birth_date": "1990-01-15",
                                                    "birth_place": "TOSHKENT SHAHAR",
                                                    "sex": "M",
                                                    "doc_give_place": "TOSHKENT SHAHAR IIB",
                                                    "issued_date": "2020-05-20",
                                                    "expiry_date": "2030-05-20",
                                                    "document": "AB1234567",
                                                    "nationality": "O'ZBEKISTON",
                                                    "photo": "base64_encoded_photo_string"
                                                  }
                                                ]
                                              },
                                              "address": {
                                                "status": 1,
                                                "data": {
                                                  "permanent_registration": {
                                                    "region": "TOSHKENT SHAHAR",
                                                    "district": "YUNUSOBOD TUMANI",
                                                    "address": "AMIR TEMUR SHOX KO'CHASI, 1-UY"
                                                  }
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - Captcha noto'g'ri yoki parametrlar xato",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Captcha",
                                    value = """
                                            {
                                              "success": false,
                                              "data": "Invalid captcha!"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autentifikatsiya xatosi - Token yo'q yoki noto'g'ri"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topilmadi - Passport ma'lumoti GUVD bazasida mavjud emas"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server xatosi - GUVD API bilan bog'lanishda xatolik",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Service Error",
                                    value = """
                                            {
                                              "success": false,
                                              "data": "apimgw.egov.uz"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/getDataBySN")
    public ResponseEntity<Object> getDataBySN(
            @Parameter(description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)", required = true, example = "12345678901234")
            @RequestParam String pinfl,

            @Parameter(description = "Passport seria va raqam (masalan: AB1234567)", required = true, example = "AB1234567")
            @RequestParam String seriaNumber,

            @Parameter(description = "Captcha identifikatori (GET /services/captcha/getNumericCaptcha)", required = true, example = "f441163e-8291-0498-730b-0b0d83b4800b")
            @RequestParam String captchaId,

            @Parameter(description = "Foydalanuvchi tomonidan kiritilgan captcha qiymati", required = true, example = "12345")
            @RequestParam String captchaValue
    ) {
        log.info("🔍 GET /app/rest/v2/services/passport-data/getDataBySN - pinfl={}, seriaNumber={}", pinfl, seriaNumber);

        // 1. Validate captcha
        if (!captchaService.validateCaptcha(captchaId, captchaValue)) {
            log.warn("⚠️ Invalid captcha: captchaId={}", captchaId);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", "Invalid captcha!");
            result.putNull("address");  // Explicitly include null (old-hemis compatible)
            return ResponseEntity.badRequest().body(result);
        }

        // 2. Get GUVD OAuth2 token
        String guvdToken;
        try {
            guvdToken = guvdTokenService.getToken();
            if (guvdToken == null) {
                log.error("❌ Failed to get GUVD token");
                ObjectNode result = objectMapper.createObjectNode();
                result.put("success", false);
                result.put("data", "GUVD token service unavailable");
                result.putNull("address");
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception e) {
            log.error("❌ Error getting GUVD token", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            result.putNull("address");
            return ResponseEntity.status(500).body(result);
        }

        // 3. Call GUVD Passport API
        try {
            // Build request body (same format as old-hemis)
            String requestBody = String.format("""
                    {
                        "transaction_id": 1,
                        "is_consent": "Y",
                        "sender_pinfl": "12345678901234",
                        "langId": 3,
                        "document": "%s",
                        "pinpp": "%s",
                        "is_photo": "Y",
                        "Sender": "M"
                    }
                    """, seriaNumber, pinfl);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + guvdToken);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("📤 Calling GUVD Passport API: {}", guvdPassportApiUrl);
            log.debug("Request body: {}", requestBody);

            // Call GUVD API
            ResponseEntity<Map> passportResponse = restTemplate.postForEntity(
                    guvdPassportApiUrl,
                    entity,
                    Map.class
            );

            Map<String, Object> passportData = passportResponse.getBody();

            // 4. Get address data
            Object addressData = getAddress(pinfl, guvdToken);

            // 5. Build response (same format as old-hemis)
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);
            result.putPOJO("data", passportData);
            if (addressData != null) {
                result.putPOJO("address", addressData);
            } else {
                result.putNull("address");
            }

            log.info("✅ Successfully retrieved passport data for PINFL: {}", pinfl);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error calling GUVD API", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            result.putNull("address");
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Seria/raqam va tug'ilgan sana bilan passport ma'lumotini olish
     * <p>
     * Old-hemis endpoint: GET /app/rest/v2/services/passport-data/getDataBySNBirthdate
     * </p>
     *
     * @param seriaNumber  Passport seria va raqam (masalan: AA6970877)
     * @param birthdate    Tug'ilgan sana (format: yyyy-MM-dd)
     * @param captchaId    Captcha identifikatori
     * @param captchaValue Foydalanuvchi tomonidan kiritilgan captcha qiymati
     * @return Passport ma'lumotlari va manzil
     */
    @Operation(
            summary = "Passport ma'lumotlarni olish (Seria nomer va tug'ilgan kun bilan)",
            description = """
                    Passport seria-raqam va tug'ilgan sana orqali GUVD bazasidan passport ma'lumotlarini olish.

                    **Talab:**
                    - seriaNumber: Passport seria va raqam (masalan: AA6970877)
                    - birthdate: Tug'ilgan sana (format: yyyy-MM-dd, masalan: 1997-07-15)
                    - captchaId: Captcha identifikatori (GET /services/captcha/getNumericCaptcha)
                    - captchaValue: Foydalanuvchi tomonidan kiritilgan captcha qiymati

                    **Response:**
                    - success: true/false
                    - data: GUVD dan kelgan passport ma'lumotlari
                    - address: Ro'yxatga olingan manzil ma'lumotlari (PINFL passport dan olinadi)

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /app/rest/v2/services/passport-data/getDataBySNBirthdate
                    **Auth:** Bearer token (required)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Passport ma'lumotlari topildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "status": 1,
                                                "error": null,
                                                "data": [
                                                  {
                                                    "current_pinpp": "12345678901234",
                                                    "sur_name_latin": "ABDULLAYEV",
                                                    "name_latin": "AKMAL",
                                                    "patronym_name_latin": "AHMADOVICH",
                                                    "birth_date": "1997-07-15",
                                                    "birth_place": "TOSHKENT SHAHAR",
                                                    "sex": "M",
                                                    "doc_give_place": "TOSHKENT SHAHAR IIB",
                                                    "issued_date": "2020-05-20",
                                                    "expiry_date": "2030-05-20",
                                                    "document": "AA6970877",
                                                    "nationality": "O'ZBEKISTON",
                                                    "photo": "base64_encoded_photo_string"
                                                  }
                                                ]
                                              },
                                              "address": {
                                                "status": 1,
                                                "data": {
                                                  "permanent_registration": {
                                                    "region": "TOSHKENT SHAHAR",
                                                    "district": "YUNUSOBOD TUMANI",
                                                    "address": "AMIR TEMUR SHOX KO'CHASI, 1-UY"
                                                  }
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - Captcha noto'g'ri yoki parametrlar xato",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Captcha",
                                    value = """
                                            {
                                              "success": false,
                                              "data": "Invalid captcha!",
                                              "address": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autentifikatsiya xatosi - Token yo'q yoki noto'g'ri"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topilmadi - Passport ma'lumoti GUVD bazasida mavjud emas"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server xatosi - GUVD API bilan bog'lanishda xatolik",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Service Error",
                                    value = """
                                            {
                                              "success": false,
                                              "data": "apimgw.egov.uz",
                                              "address": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/getDataBySNBirthdate")
    public ResponseEntity<Object> getDataBySNBirthdate(
            @Parameter(description = "Passport seria va raqam (masalan: AA6970877)", required = true, example = "AA1234567")
            @RequestParam String seriaNumber,

            @Parameter(description = "Tug'ilgan sana (format: yyyy-MM-dd)", required = true, example = "1990-01-01")
            @RequestParam String birthdate,

            @Parameter(description = "Captcha identifikatori (GET /services/captcha/getNumericCaptcha)", required = true, example = "f441163e-8291-0498-730b-0b0d83b4800b")
            @RequestParam String captchaId,

            @Parameter(description = "Foydalanuvchi tomonidan kiritilgan captcha qiymati", required = true, example = "12345")
            @RequestParam String captchaValue
    ) {
        log.info("🔍 GET /app/rest/v2/services/passport-data/getDataBySNBirthdate - seriaNumber={}, birthdate={}", seriaNumber, birthdate);

        // 1. Validate captcha
        if (!captchaService.validateCaptcha(captchaId, captchaValue)) {
            log.warn("⚠️ Invalid captcha: captchaId={}", captchaId);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", "Invalid captcha!");
            result.putNull("address");  // Explicitly include null (old-hemis compatible)
            return ResponseEntity.badRequest().body(result);
        }

        // 2. Get GUVD OAuth2 token
        String guvdToken;
        try {
            guvdToken = guvdTokenService.getToken();
            if (guvdToken == null) {
                log.error("❌ Failed to get GUVD token");
                ObjectNode result = objectMapper.createObjectNode();
                result.put("success", false);
                result.put("data", "GUVD token service unavailable");
                result.putNull("address");
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception e) {
            log.error("❌ Error getting GUVD token", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            result.putNull("address");
            return ResponseEntity.status(500).body(result);
        }

        // 3. Call GUVD Passport API (with document + birth_date, NO pinpp!)
        try {
            // Build request body (same format as old-hemis - NO pinpp field!)
            String requestBody = String.format("""
                    {
                        "transaction_id": 1,
                        "is_consent": "Y",
                        "sender_pinfl": "12345678901234",
                        "langId": 3,
                        "document": "%s",
                        "birth_date": "%s",
                        "is_photo": "Y",
                        "Sender": "M"
                    }
                    """, seriaNumber, birthdate);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + guvdToken);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("📤 Calling GUVD Passport API: {}", guvdPassportApiUrl);
            log.debug("Request body: {}", requestBody);

            // Call GUVD API
            ResponseEntity<Map> passportResponse = restTemplate.postForEntity(
                    guvdPassportApiUrl,
                    entity,
                    Map.class
            );

            Map<String, Object> passportData = passportResponse.getBody();

            // 4. Build response
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);
            result.putPOJO("data", passportData);

            // 5. Get address data (extract PINFL from passport response first)
            // Old-hemis logic: if passport.data[0].current_pinpp exists, get address
            Object addressData = null;
            if (passportData != null && passportData.containsKey("data")) {
                try {
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) passportData.get("data");
                    if (dataList != null && !dataList.isEmpty()) {
                        Map<String, Object> firstData = dataList.get(0);
                        if (firstData.containsKey("current_pinpp")) {
                            String pinfl = (String) firstData.get("current_pinpp");
                            addressData = getAddress(pinfl, guvdToken);
                        }
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Could not extract PINFL from passport data for address lookup", e);
                }
            }

            if (addressData != null) {
                result.putPOJO("address", addressData);
            } else {
                result.putNull("address");
            }

            log.info("✅ Successfully retrieved passport data for seriaNumber={}, birthdate={}", seriaNumber, birthdate);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error calling GUVD API", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            result.putNull("address");
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Get address data from GUVD Address API
     *
     * <p>Old-hemis compatible: returns error object on failure, not null</p>
     *
     * @param pinfl      PINFL
     * @param guvdToken  GUVD OAuth2 token
     * @return Address data or error object (never null for old-hemis compatibility)
     */
    private Object getAddress(String pinfl, String guvdToken) {
        try {
            // Build request body
            String requestBody = String.format("""
                    {
                        "pinpp": "%s"
                    }
                    """, pinfl);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + guvdToken);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("📤 Calling GUVD Address API: {}", guvdAddressApiUrl);

            // Call GUVD Address API
            ResponseEntity<Map> addressResponse = restTemplate.postForEntity(
                    guvdAddressApiUrl,
                    entity,
                    Map.class
            );

            return addressResponse.getBody();

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // GUVD API returned error response - extract and return the error body directly
            log.error("⚠️ GUVD Address API returned error: {}", e.getStatusCode());
            try {
                // Parse the error response body from GUVD
                return objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
            } catch (Exception parseEx) {
                // If parsing fails, create error object
                log.error("⚠️ Failed to parse GUVD error response", parseEx);
                return createAddressErrorObject(e.getMessage());
            }
        } catch (Exception e) {
            log.error("⚠️ Error getting address data", e);
            return createAddressErrorObject(e.getMessage());
        }
    }

    /**
     * PINFL va tug'ilgan sana bilan passport ma'lumotini olish
     * <p>
     * Old-hemis endpoint: GET /app/rest/v2/services/passport-data/getDataByPinflBirthdate
     * </p>
     *
     * @param pinfl        PINFL (14 raqamli)
     * @param birthdate    Tug'ilgan sana (format: yyyy-MM-dd)
     * @param captchaId    Captcha identifikatori
     * @param captchaValue Foydalanuvchi tomonidan kiritilgan captcha qiymati
     * @return Passport ma'lumotlari va manzil
     */
    @Operation(
            summary = "Passport ma'lumotlarni olish (Pinfl va tug'ilgan kun bilan)",
            description = """
                    PINFL va tug'ilgan sana orqali GUVD bazasidan passport ma'lumotlarini olish.

                    **Talab:**
                    - pinfl: PINFL (14 raqamli shaxsiy identifikatsiya raqami)
                    - birthdate: Tug'ilgan sana (format: yyyy-MM-dd, masalan: 1997-07-15)
                    - captchaId: Captcha identifikatori (GET /services/captcha/getNumericCaptcha)
                    - captchaValue: Foydalanuvchi tomonidan kiritilgan captcha qiymati

                    **Response:**
                    - success: true/false
                    - data: GUVD dan kelgan passport ma'lumotlari
                    - address: Ro'yxatga olingan manzil ma'lumotlari

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /app/rest/v2/services/passport-data/getDataByPinflBirthdate
                    **Auth:** Bearer token (required)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Passport ma'lumotlari topildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "status": 1,
                                                "error": null,
                                                "data": [
                                                  {
                                                    "current_pinpp": "31507976020031",
                                                    "sur_name_latin": "ABDULLAYEV",
                                                    "name_latin": "AKMAL",
                                                    "patronym_name_latin": "AHMADOVICH",
                                                    "birth_date": "1997-07-15",
                                                    "birth_place": "TOSHKENT SHAHAR",
                                                    "sex": "M",
                                                    "doc_give_place": "TOSHKENT SHAHAR IIB",
                                                    "issued_date": "2020-05-20",
                                                    "expiry_date": "2030-05-20",
                                                    "document": "AB1234567",
                                                    "nationality": "O'ZBEKISTON",
                                                    "photo": "base64_encoded_photo_string"
                                                  }
                                                ]
                                              },
                                              "address": {
                                                "status": 1,
                                                "data": {
                                                  "permanent_registration": {
                                                    "region": "TOSHKENT SHAHAR",
                                                    "district": "YUNUSOBOD TUMANI",
                                                    "address": "AMIR TEMUR SHOX KO'CHASI, 1-UY"
                                                  }
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - Captcha noto'g'ri yoki parametrlar xato",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Captcha",
                                    value = """
                                            {
                                              "success": false,
                                              "data": "Invalid captcha!",
                                              "address": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autentifikatsiya xatosi - Token yo'q yoki noto'g'ri"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Topilmadi - Passport ma'lumoti GUVD bazasida mavjud emas"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server xatosi - GUVD API bilan bog'lanishda xatolik",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Service Error",
                                    value = """
                                            {
                                              "success": false,
                                              "data": "apimgw.egov.uz",
                                              "address": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/getDataByPinflBirthdate")
    public ResponseEntity<Object> getDataByPinflBirthdate(
            @Parameter(description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)", required = true, example = "31507976020031")
            @RequestParam String pinfl,

            @Parameter(description = "Tug'ilgan sana (format: yyyy-MM-dd)", required = true, example = "1997-07-15")
            @RequestParam String birthdate,

            @Parameter(description = "Captcha identifikatori (GET /services/captcha/getNumericCaptcha)", required = true, example = "f441163e-8291-0498-730b-0b0d83b4800b")
            @RequestParam String captchaId,

            @Parameter(description = "Foydalanuvchi tomonidan kiritilgan captcha qiymati", required = true, example = "12345")
            @RequestParam String captchaValue
    ) {
        log.info("🔍 GET /app/rest/v2/services/passport-data/getDataByPinflBirthdate - pinfl={}, birthdate={}", pinfl, birthdate);

        // 1. Validate captcha
        if (!captchaService.validateCaptcha(captchaId, captchaValue)) {
            log.warn("⚠️ Invalid captcha: captchaId={}", captchaId);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", "Invalid captcha!");
            result.putNull("address");  // Explicitly include null (old-hemis compatible)
            return ResponseEntity.badRequest().body(result);
        }

        // 2. Get GUVD OAuth2 token
        String guvdToken;
        try {
            guvdToken = guvdTokenService.getToken();
            if (guvdToken == null) {
                log.error("❌ Failed to get GUVD token");
                ObjectNode result = objectMapper.createObjectNode();
                result.put("success", false);
                result.put("data", "GUVD token service unavailable");
                result.putNull("address");
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception e) {
            log.error("❌ Error getting GUVD token", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            result.putNull("address");
            return ResponseEntity.status(500).body(result);
        }

        // 3. Call GUVD Passport API (with pinpp + birth_date, NO document!)
        try {
            // Build request body (same format as old-hemis - pinpp + birth_date, NO document field!)
            String requestBody = String.format("""
                    {
                        "transaction_id": 1,
                        "is_consent": "Y",
                        "sender_pinfl": "12345678901234",
                        "langId": 3,
                        "pinpp": "%s",
                        "birth_date": "%s",
                        "is_photo": "Y",
                        "Sender": "M"
                    }
                    """, pinfl, birthdate);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + guvdToken);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("📤 Calling GUVD Passport API: {}", guvdPassportApiUrl);
            log.debug("Request body: {}", requestBody);

            // Call GUVD API
            ResponseEntity<Map> passportResponse = restTemplate.postForEntity(
                    guvdPassportApiUrl,
                    entity,
                    Map.class
            );

            Map<String, Object> passportData = passportResponse.getBody();

            // 4. Get address data (PINFL already provided in request)
            Object addressData = getAddress(pinfl, guvdToken);

            // 5. Build response (same format as old-hemis)
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);
            result.putPOJO("data", passportData);
            if (addressData != null) {
                result.putPOJO("address", addressData);
            } else {
                result.putNull("address");
            }

            log.info("✅ Successfully retrieved passport data for PINFL: {}, birthdate: {}", pinfl, birthdate);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error calling GUVD API", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            result.putNull("address");
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * PINFL orqali manzil ma'lumotlarini olish (public endpoint)
     * <p>
     * Old-hemis endpoint: GET /app/rest/v2/services/passport-data/getAddress
     * </p>
     *
     * @param pinfl PINFL (14 raqamli)
     * @return Manzil ma'lumotlari {success, data} formatida
     */
    @Operation(
            summary = "Address",
            description = """
                    PINFL orqali GUVD bazasidan manzil ma'lumotlarini olish.

                    **Talab:**
                    - pinfl: PINFL (14 raqamli shaxsiy identifikatsiya raqami)

                    **Response:**
                    - success: true/false
                    - data: GUVD dan kelgan manzil ma'lumotlari yoki xato xabari

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /app/rest/v2/services/passport-data/getAddress
                    **Auth:** Bearer token (required)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Muvaffaqiyatli - Manzil ma'lumotlari topildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "status": 1,
                                                "data": {
                                                  "permanent_registration": {
                                                    "region": "TOSHKENT SHAHAR",
                                                    "district": "YUNUSOBOD TUMANI",
                                                    "address": "AMIR TEMUR SHOX KO'CHASI, 1-UY"
                                                  }
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autentifikatsiya xatosi - Token yo'q yoki noto'g'ri"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server xatosi - GUVD API bilan bog'lanishda xatolik",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Service Error",
                                    value = """
                                            {
                                              "success": false,
                                              "data": "apimgw.egov.uz"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/getAddress")
    public ResponseEntity<Object> getAddressPublic(
            @Parameter(description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)", required = true, example = "12345678901234")
            @RequestParam String pinfl
    ) {
        log.info("🔍 GET /app/rest/v2/services/passport-data/getAddress - pinfl={}", pinfl);

        // 1. Get GUVD OAuth2 token
        String guvdToken;
        try {
            guvdToken = guvdTokenService.getToken();
            if (guvdToken == null) {
                log.error("❌ Failed to get GUVD token");
                ObjectNode result = objectMapper.createObjectNode();
                result.put("success", false);
                result.put("data", "GUVD token service unavailable");
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception e) {
            log.error("❌ Error getting GUVD token", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }

        // 2. Call GUVD Address API
        try {
            // Build request body
            String requestBody = String.format("""
                    {
                        "pinpp": "%s"
                    }
                    """, pinfl);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + guvdToken);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("📤 Calling GUVD Address API: {}", guvdAddressApiUrl);

            // Call GUVD Address API
            ResponseEntity<Map> addressResponse = restTemplate.postForEntity(
                    guvdAddressApiUrl,
                    entity,
                    Map.class
            );

            // 3. Build response (same format as old-hemis: {success, data})
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);
            result.putPOJO("data", addressResponse.getBody());

            log.info("✅ Successfully retrieved address data for PINFL: {}", pinfl);
            return ResponseEntity.ok(result);

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            // GUVD API returned error response
            log.error("⚠️ GUVD Address API returned error: {}", e.getStatusCode());
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            return ResponseEntity.ok(result);  // Return 200 with success=false (old-hemis compatible)

        } catch (Exception e) {
            log.error("❌ Error calling GUVD Address API", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            return ResponseEntity.ok(result);  // Return 200 with success=false (old-hemis compatible)
        }
    }

    /**
     * Create address error object (old-hemis compatible format)
     */
    private ObjectNode createAddressErrorObject(String message) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("code", 500);
        errorNode.put("message", message);
        errorNode.put("path", "/api/getPersonRegistrations");
        errorNode.put("timestamp", java.time.ZonedDateTime.now(
                java.time.ZoneId.of("Asia/Tashkent")
        ).format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm:ss a 'UTC'XXX", java.util.Locale.ENGLISH)));
        return errorNode;
    }
}
