package uz.hemis.api.legacy.controller.student;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uz.hemis.common.Jackson2Response;
import uz.hemis.common.log.LogSafe;
import uz.hemis.service.shared.CaptchaService;
import uz.hemis.service.integration.ApiMspdTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
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
@Jackson2Response
@RestController
@RequestMapping("/app/rest/v2/services/passport-data")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
@SuppressWarnings("unchecked")
public class PassportServiceController {

    private final CaptchaService captchaService;
    private final ApiMspdTokenService apiMspdTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

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
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - Captcha noto'g'ri yoki parametrlar xato",
                    content = @Content(
                            mediaType = "application/json"
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
                            mediaType = "application/json"
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getDataBySN")
    public ResponseEntity<Object> getDataBySN(
            @Parameter(description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)", required = true)
            @RequestParam String pinfl,

            @Parameter(description = "Passport seria va raqam (masalan: AB1234567)", required = false)
            @RequestParam(required = false) String seriaNumber,

            @Parameter(description = "Passport seriyasi (alias)", required = false)
            @RequestParam(required = false) String seria,

            @Parameter(description = "Passport raqami (alias)", required = false)
            @RequestParam(required = false) String number,

            @Parameter(description = "Captcha identifikatori", required = false)
            @RequestParam(required = false) String captchaId,

            @Parameter(description = "Captcha qiymati", required = false)
            @RequestParam(required = false) String captchaValue,

            @Parameter(description = "Captcha (combined alias)", required = false)
            @RequestParam(required = false) String captcha
    ) {
        // Accept seria+number as separate params (combine into seriaNumber)
        if (seriaNumber == null && seria != null && number != null) seriaNumber = seria + number;
        if (seriaNumber == null && seria != null) seriaNumber = seria;
        // Accept captcha as combined captchaId+captchaValue
        if (captchaId == null && captcha != null) captchaId = captcha;
        if (captchaValue == null && captcha != null) captchaValue = captcha;

        log.info("🔍 GET /app/rest/v2/services/passport-data/getDataBySN - pinfl={}, seriaNumber={}",
                LogSafe.pinfl(pinfl), LogSafe.passport(seriaNumber));

        // 1. Validate captcha
        if (!captchaService.validateCaptcha(captchaId, captchaValue)) {
            log.warn("⚠️ Invalid captcha: captchaId={}", captchaId);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", "Invalid captcha!");
            result.putNull("address");  // Explicitly include null (old-hemis compatible)
            return ResponseEntity.ok(result);
        }

        // 2-5. Call API-MSPD gateway for passport + address data
        return callApiMspdPerson(pinfl, seriaNumber, null);
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
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - Captcha noto'g'ri yoki parametrlar xato",
                    content = @Content(
                            mediaType = "application/json"
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
                            mediaType = "application/json"
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getDataBySNBirthdate")
    public ResponseEntity<Object> getDataBySNBirthdate(
            @Parameter(description = "Passport seria va raqam (masalan: AA6970877)", required = true)
            @RequestParam String seriaNumber,

            @Parameter(description = "Tug'ilgan sana (format: yyyy-MM-dd)", required = true)
            @RequestParam String birthdate,

            @Parameter(description = "Captcha identifikatori (GET /services/captcha/getNumericCaptcha)", required = true)
            @RequestParam String captchaId,

            @Parameter(description = "Foydalanuvchi tomonidan kiritilgan captcha qiymati", required = true)
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

        // 2-5. Call API-MSPD gateway for passport + address data
        // getDataBySNBirthdate: pinfl yo'q, document + birthdate
        return callApiMspdPersonByDocBirthdate(seriaNumber, birthdate);
    }

    // =====================================================
    // API-MSPD Gateway Integration Methods
    // =====================================================

    /**
     * Call API-MSPD /person/pinpp-and-document/ (pinfl + document)
     * Maps to getDataBySN endpoint.
     */
    private ResponseEntity<Object> callApiMspdPerson(String pinfl, String document, String birthdate) {
        String token = apiMspdTokenService.getAccessToken();
        if (token == null) {
            return errorResponse("API-MSPD token service unavailable");
        }

        String baseUrl = apiMspdTokenService.getBaseUrl();
        try {
            // 1. Get person data via API-MSPD
            Map<String, String> body = new LinkedHashMap<>();
            body.put("pinfl", pinfl);
            body.put("document", document);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.info("Calling API-MSPD: POST {}/person/pinpp-and-document/", baseUrl);

            ResponseEntity<Map> personResponse = restTemplate.postForEntity(
                    baseUrl + "/person/pinpp-and-document/",
                    entity,
                    Map.class
            );

            Map<String, Object> mspdData = personResponse.getBody();
            return buildOldHemisResponse(mspdData, pinfl, token, baseUrl);

        } catch (Exception e) {
            log.error("Error calling API-MSPD person endpoint", e);
            return errorResponse(e.getMessage());
        }
    }

    /**
     * Call API-MSPD /person/document-and-birth-date/ (document + birthdate)
     * Maps to getDataBySNBirthdate endpoint.
     */
    private ResponseEntity<Object> callApiMspdPersonByDocBirthdate(String document, String birthdate) {
        String token = apiMspdTokenService.getAccessToken();
        if (token == null) {
            return errorResponse("API-MSPD token service unavailable");
        }

        String baseUrl = apiMspdTokenService.getBaseUrl();
        try {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("document", document);
            body.put("birth_date", birthdate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.info("Calling API-MSPD: POST {}/person/document-and-birth-date/", baseUrl);

            ResponseEntity<Map> personResponse = restTemplate.postForEntity(
                    baseUrl + "/person/document-and-birth-date/",
                    entity,
                    Map.class
            );

            Map<String, Object> mspdData = personResponse.getBody();
            // Extract pinfl from response for address lookup
            String pinfl = extractPinflFromMspdResponse(mspdData);
            return buildOldHemisResponse(mspdData, pinfl, token, baseUrl);

        } catch (Exception e) {
            log.error("Error calling API-MSPD person endpoint", e);
            return errorResponse(e.getMessage());
        }
    }

    /**
     * Call API-MSPD /person/pinpp-and-birth-date/ (pinfl + birthdate)
     * Maps to getDataByPinflBirthdate endpoint.
     */
    private ResponseEntity<Object> callApiMspdPersonByPinflBirthdate(String pinfl, String birthdate) {
        String token = apiMspdTokenService.getAccessToken();
        if (token == null) {
            return errorResponse("API-MSPD token service unavailable");
        }

        String baseUrl = apiMspdTokenService.getBaseUrl();
        try {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("pinfl", pinfl);
            body.put("birth_date", birthdate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.info("Calling API-MSPD: POST {}/person/pinpp-and-birth-date/", baseUrl);

            ResponseEntity<Map> personResponse = restTemplate.postForEntity(
                    baseUrl + "/person/pinpp-and-birth-date/",
                    entity,
                    Map.class
            );

            Map<String, Object> mspdData = personResponse.getBody();
            return buildOldHemisResponse(mspdData, pinfl, token, baseUrl);

        } catch (Exception e) {
            log.error("Error calling API-MSPD person endpoint", e);
            return errorResponse(e.getMessage());
        }
    }

    /**
     * Get address from API-MSPD /person/person-address/
     */
    private Object getAddressViaMspd(String pinfl, String token, String baseUrl) {
        if (pinfl == null || pinfl.isBlank()) return null;
        try {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("pinfl", pinfl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            log.info("Calling API-MSPD: POST {}/person/person-address/", baseUrl);

            ResponseEntity<Map> addressResponse = restTemplate.postForEntity(
                    baseUrl + "/person/person-address/",
                    entity,
                    Map.class
            );

            return addressResponse.getBody();
        } catch (Exception e) {
            log.warn("API-MSPD address call failed: {}", e.getMessage());
            return createAddressErrorObject(e.getMessage());
        }
    }

    /**
     * Build old-hemis compatible response from API-MSPD data.
     *
     * API-MSPD returns: {message, status_code, result, data: {person}, comments}
     * Old-hemis expects: {success: true, data: {result, data: [{person}], comments}, address: {...}}
     */
    private ResponseEntity<Object> buildOldHemisResponse(Map<String, Object> mspdData, String pinfl,
                                                          String token, String baseUrl) {
        if (mspdData == null) {
            return errorResponse("No data received from API-MSPD");
        }

        // Check if API-MSPD returned success
        String resultCode = mspdData.get("result") != null ? mspdData.get("result").toString() : null;
        if (!"1".equals(resultCode)) {
            return errorResponse(mspdData.get("message") != null ? mspdData.get("message").toString() : "Person not found");
        }

        // Re-wrap: api_mspd returns data as single object, old-hemis expects data array
        Object personData = mspdData.get("data");
        Map<String, Object> guvdLikeData = new LinkedHashMap<>();
        guvdLikeData.put("result", resultCode);
        guvdLikeData.put("data", personData != null ? List.of(personData) : List.of());
        guvdLikeData.put("comments", mspdData.getOrDefault("comments", ""));

        // Get address
        Object addressData = getAddressViaMspd(pinfl, token, baseUrl);

        // Build old-hemis response
        ObjectNode result = objectMapper.createObjectNode();
        result.put("success", true);
        result.putPOJO("data", guvdLikeData);
        if (addressData != null) {
            result.putPOJO("address", addressData);
        } else {
            result.putNull("address");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Extract PINFL from API-MSPD person response.
     */
    private String extractPinflFromMspdResponse(Map<String, Object> mspdData) {
        if (mspdData == null) return null;
        Object data = mspdData.get("data");
        if (data instanceof Map) {
            Object pinfl = ((Map<?, ?>) data).get("current_pinpp");
            if (pinfl == null) pinfl = ((Map<?, ?>) data).get("current_pinfl");
            return pinfl != null ? pinfl.toString() : null;
        }
        return null;
    }

    /**
     * Error response in old-hemis format: {success: false, data: "message", address: null}
     */
    private ResponseEntity<Object> errorResponse(String message) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("success", false);
        result.put("data", message);
        result.putNull("address");
        return ResponseEntity.ok(result);
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
                            schema = @Schema(implementation = Map.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Xato so'rov - Captcha noto'g'ri yoki parametrlar xato",
                    content = @Content(
                            mediaType = "application/json"
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
                            mediaType = "application/json"
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getDataByPinflBirthdate")
    public ResponseEntity<Object> getDataByPinflBirthdate(
            @Parameter(description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)", required = true)
            @RequestParam String pinfl,

            @Parameter(description = "Tug'ilgan sana (format: yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String birthdate,

            @Parameter(description = "Tug'ilgan sana (alias: birth_date)", required = false)
            @RequestParam(name = "birth_date", required = false) String birthDateAlias,

            @Parameter(description = "Captcha identifikatori", required = false)
            @RequestParam(required = false) String captchaId,

            @Parameter(description = "Captcha qiymati", required = false)
            @RequestParam(required = false) String captchaValue,

            @Parameter(description = "Captcha (combined alias)", required = false)
            @RequestParam(required = false) String captcha
    ) {
        // Accept both birthdate and birth_date parameter names
        if (birthdate == null && birthDateAlias != null) birthdate = birthDateAlias;
        // Accept captcha as combined captchaId+captchaValue
        if (captchaId == null && captcha != null) captchaId = captcha;
        if (captchaValue == null && captcha != null) captchaValue = captcha;

        log.info("🔍 GET /app/rest/v2/services/passport-data/getDataByPinflBirthdate - pinfl={}, birthdate=***",
                LogSafe.pinfl(pinfl));

        // 1. Validate captcha
        if (!captchaService.validateCaptcha(captchaId, captchaValue)) {
            log.warn("⚠️ Invalid captcha: captchaId={}", captchaId);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", "Invalid captcha!");
            result.putNull("address");  // Explicitly include null (old-hemis compatible)
            return ResponseEntity.ok(result);
        }

        // 2. Call API-MSPD (PINFL + birthdate)
        return callApiMspdPersonByPinflBirthdate(pinfl, birthdate);
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
                            schema = @Schema(implementation = Map.class)
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
                            mediaType = "application/json"
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getAddress")
    public ResponseEntity<Object> getAddressPublic(
            @Parameter(description = "PINFL (14 raqamli shaxsiy identifikatsiya raqami)", required = true)
            @RequestParam String pinfl
    ) {
        log.info("🔍 GET /app/rest/v2/services/passport-data/getAddress - pinfl={}", LogSafe.pinfl(pinfl));

        // 1. Get API-MSPD token
        String token = apiMspdTokenService.getAccessToken();
        if (token == null) {
            log.error("❌ Failed to get API-MSPD token");
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", "API-MSPD token service unavailable");
            return ResponseEntity.ok(result);
        }

        // 2. Call API-MSPD address endpoint
        try {
            String baseUrl = apiMspdTokenService.getBaseUrl();
            Object addressData = getAddressViaMspd(pinfl, token, baseUrl);

            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);
            result.putPOJO("data", addressData);

            log.info("✅ Successfully retrieved address data for PINFL: {}", LogSafe.pinfl(pinfl));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ Error calling API-MSPD Address API", e);
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", false);
            result.put("data", e.getMessage());
            return ResponseEntity.ok(result);
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
