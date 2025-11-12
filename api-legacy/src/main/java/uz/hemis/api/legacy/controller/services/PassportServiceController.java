package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.integration.PassportIntegrationService;

import java.util.Map;

/**
 * Passport Service Controller - GUVD Integration
 *
 * <p><strong>URL Pattern:</strong> {@code /services/passport-data/*} and {@code /services/personal-data/*}</p>
 *
 * <p><strong>Legacy Compatibility:</strong></p>
 * <ul>
 *   <li>Matches OLD-HEMIS CUBA service pattern</li>
 *   <li>Integrates with GUVD (МВД) passport database</li>
 *   <li>Provides passport verification and data retrieval</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /services/passport-data/getData - Get passport by PINFL</li>
 *   <li>GET /services/passport-data/getDataBySN - Get by PINFL + seria/number</li>
 *   <li>GET /services/passport-data/getDataBySNBirthdate - Get by seria/number + birthdate</li>
 *   <li>GET /services/passport-data/getDataByPinflBirthdate - Get by PINFL + birthdate</li>
 *   <li>GET /services/passport-data/getAddress - Get address by PINFL</li>
 *   <li>GET /services/personal-data/getData - Legacy endpoint (deprecated)</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "02. Passport ma'lumotlari", description = "GUVD passport ma'lumotlarini olish va tekshirish xizmatlari")
@RestController
@RequestMapping("/app/rest/v2/services")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PassportServiceController {

    private final PassportIntegrationService passportIntegrationService;

    /**
     * Get passport data by PINFL (new version)
     *
     * <p><strong>Endpoint:</strong> GET /services/passport-data/getData</p>
     *
     * @param pinfl Person PINFL (14 digits)
     * @param givenDate Passport given date (optional)
     * @return Passport information from GUVD database
     */
    @Operation(
        summary = "Passport ma'lumotlarini olish (yangi)",
        description = "PINFL va passport berilgan sana orqali GUVD bazasidan ma'lumot olish"
    )
    @GetMapping("/passport-data/getData")
    public ResponseEntity<Map<String, Object>> getPassportData(
        @Parameter(description = "PINFL (14 raqam)", required = true, example = "31503776560016")
        @RequestParam String pinfl,
        
        @Parameter(description = "Passport berilgan sana (yyyy-MM-dd)", example = "2015-05-15")
        @RequestParam(required = false) String givenDate
    ) {
        log.info("GET /app/rest/v2/services/passport-data/getData - pinfl: {}, givenDate: {}", pinfl, givenDate);
        return ResponseEntity.ok(passportIntegrationService.getPassportDataByPinfl(pinfl, givenDate));
    }

    /**
     * Get passport data by PINFL and Serial Number
     *
     * <p><strong>Endpoint:</strong> GET /services/passport-data/getDataBySN</p>
     *
     * @param pinfl Person PINFL
     * @param seriaNumber Passport seria and number (e.g., "AA1234567")
     * @param captchaId Captcha session ID
     * @param captchaValue Captcha user input
     * @return Passport information
     */
    @Operation(
        summary = "Passport ma'lumotlarini olish (PINFL va seria-raqam bilan)",
        description = "PINFL va passport seria-raqami orqali ma'lumot olish (captcha talab qilinadi)"
    )
    @GetMapping("/passport-data/getDataBySN")
    public ResponseEntity<Map<String, Object>> getPassportDataBySerialNumber(
        @Parameter(description = "PINFL", required = true, example = "31503776560016")
        @RequestParam String pinfl,
        
        @Parameter(description = "Passport seria va raqami", required = true, example = "AA1234567")
        @RequestParam String seriaNumber,
        
        @Parameter(description = "Captcha ID", required = false)
        @RequestParam(required = false) String captchaId,
        
        @Parameter(description = "Captcha qiymati", required = false)
        @RequestParam(required = false) String captchaValue
    ) {
        log.info("GET /services/passport-data/getDataBySN - pinfl: {}, seriaNumber: {}", pinfl, seriaNumber);
        return ResponseEntity.ok(passportIntegrationService.getPassportDataBySerialNumber(
            pinfl, seriaNumber, captchaId, captchaValue));
    }

    /**
     * Get passport data by Serial Number and Birthdate
     *
     * <p><strong>Endpoint:</strong> GET /services/passport-data/getDataBySNBirthdate</p>
     *
     * @param seriaNumber Passport seria and number
     * @param birthdate Person birthdate (yyyy-MM-dd)
     * @param captchaId Captcha session ID
     * @param captchaValue Captcha user input
     * @return Passport information
     */
    @Operation(
        summary = "Passport ma'lumotlarini olish (seria-raqam va tug'ilgan kun bilan)",
        description = "Passport seria-raqami va tug'ilgan kun orqali ma'lumot olish"
    )
    @GetMapping("/passport-data/getDataBySNBirthdate")
    public ResponseEntity<Map<String, Object>> getPassportDataBySerialAndBirthdate(
        @Parameter(description = "Passport seria va raqami", required = true, example = "AA1234567")
        @RequestParam String seriaNumber,
        
        @Parameter(description = "Tug'ilgan sana (yyyy-MM-dd)", required = true, example = "1976-03-15")
        @RequestParam String birthdate,
        
        @Parameter(description = "Captcha ID", required = false)
        @RequestParam(required = false) String captchaId,
        
        @Parameter(description = "Captcha qiymati", required = false)
        @RequestParam(required = false) String captchaValue
    ) {
        log.info("GET /services/passport-data/getDataBySNBirthdate - seriaNumber: {}, birthdate: {}", 
            seriaNumber, birthdate);
        return ResponseEntity.ok(passportIntegrationService.getPassportDataBySerialAndBirthdate(
            seriaNumber, birthdate, captchaId, captchaValue));
    }

    /**
     * Get passport data by PINFL and Birthdate
     *
     * <p><strong>Endpoint:</strong> GET /services/passport-data/getDataByPinflBirthdate</p>
     *
     * @param pinfl Person PINFL
     * @param birthdate Person birthdate
     * @param captchaId Captcha session ID
     * @param captchaValue Captcha user input
     * @return Passport information
     */
    @Operation(
        summary = "Passport ma'lumotlarini olish (PINFL va tug'ilgan kun bilan)",
        description = "PINFL va tug'ilgan kun orqali ma'lumot olish"
    )
    @GetMapping("/passport-data/getDataByPinflBirthdate")
    public ResponseEntity<Map<String, Object>> getPassportDataByPinflAndBirthdate(
        @Parameter(description = "PINFL", required = true, example = "31503776560016")
        @RequestParam String pinfl,
        
        @Parameter(description = "Tug'ilgan sana (yyyy-MM-dd)", required = true, example = "1976-03-15")
        @RequestParam String birthdate,
        
        @Parameter(description = "Captcha ID", required = false)
        @RequestParam(required = false) String captchaId,
        
        @Parameter(description = "Captcha qiymati", required = false)
        @RequestParam(required = false) String captchaValue
    ) {
        log.info("GET /services/passport-data/getDataByPinflBirthdate - pinfl: {}, birthdate: {}", 
            pinfl, birthdate);
        return ResponseEntity.ok(passportIntegrationService.getPassportDataByPinflAndBirthdate(
            pinfl, birthdate, captchaId, captchaValue));
    }

    /**
     * Get address by PINFL
     *
     * <p><strong>Endpoint:</strong> GET /services/passport-data/getAddress</p>
     *
     * @param pinfl Person PINFL
     * @return Person address information
     */
    @Operation(
        summary = "Manzil ma'lumotlarini olish",
        description = "PINFL orqali shaxsning ro'yxatdan o'tgan manzilini olish"
    )
    @GetMapping("/passport-data/getAddress")
    public ResponseEntity<Map<String, Object>> getAddress(
        @Parameter(description = "PINFL", required = true, example = "31503776560016")
        @RequestParam String pinfl
    ) {
        log.info("GET /app/rest/v2/services/passport-data/getAddress - pinfl: {}", pinfl);
        return ResponseEntity.ok(passportIntegrationService.getAddress(pinfl));
    }

    /**
     * Get passport data (legacy endpoint)
     *
     * <p><strong>Endpoint:</strong> GET /services/personal-data/getData</p>
     * <p><strong>Status:</strong> DEPRECATED - use /passport-data/getData instead</p>
     *
     * @param pinfl Person PINFL
     * @param serial Passport serial (deprecated parameter)
     * @return Passport information
     */
    @Deprecated
    @Operation(
        summary = "Passport ma'lumotlarini olish (eski)",
        description = "Legacy endpoint - /passport-data/getData dan foydalaning",
        deprecated = true
    )
    @GetMapping("/personal-data/getData")
    public ResponseEntity<Map<String, Object>> getPersonalData(
        @Parameter(description = "PINFL", required = true, example = "31503776560016")
        @RequestParam String pinfl,
        
        @Parameter(description = "Serial (deprecated)", required = false)
        @RequestParam(required = false) String serial
    ) {
        log.warn("DEPRECATED: GET /app/rest/v2/services/personal-data/getData - pinfl: {}", pinfl);
        // Delegate to new endpoint
        return ResponseEntity.ok(passportIntegrationService.getPassportDataByPinfl(pinfl, null));
    }
}
