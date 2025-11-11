package uz.hemis.university.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.government.service.*;

import java.util.Map;

/**
 * Services Controller - Government Integration Endpoints
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Mimics CUBA Platform REST API v2 services pattern</li>
 *   <li>URL: /app/rest/v2/services/{serviceName}/{methodName}</li>
 *   <li>Example: /app/rest/v2/services/hemishe_PersonalDataService/getData</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern:</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_PersonalDataService/getData?pinfl=X&serial=Y
 * → PersonalDataService.getData(pinfl, serial)
 * </pre>
 *
 * <p><strong>Government Integration Services (20 endpoints):</strong></p>
 * <ul>
 *   <li>hemishe_PersonalDataService - Government personal data (MVD) - 2 endpoints</li>
 *   <li>hemishe_PassportDataService - Passport and address data - 4 endpoints</li>
 *   <li>hemishe_BimmService - Disability and social benefits - 5 endpoints</li>
 *   <li>hemishe_SocialService - Social registries and support programs - 6 endpoints</li>
 *   <li>hemishe_GuvdService - GUVD integration - 2 endpoints</li>
 *   <li>hemishe_TaxService - Tax service integration - 1 endpoint</li>
 * </ul>
 *
 * <p><strong>Note:</strong> University services migrated to feature-based controllers:</p>
 * <ul>
 *   <li>StudentApiController - 18 endpoints</li>
 *   <li>TeacherApiController - 4 endpoints</li>
 *   <li>ReferenceDataApiController - 4 endpoints</li>
 *   <li>ClassifierApiController - 7 endpoints</li>
 *   <li>DocumentApiController - 6 endpoints</li>
 *   <li>IntegrationApiController - 6 endpoints</li>
 *   <li>UtilityApiController - 7 endpoints</li>
 *   <li>MinorServicesApiController - 10 endpoints</li>
 *   <li>FinalServicesApiController - 4 endpoints</li>
 * </ul>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services")
@RequiredArgsConstructor
@Slf4j
public class ServicesController {

    private final PersonalDataService personalDataService;
    private final PassportDataService passportDataService;
    private final BimmService bimmService;
    private final SocialService socialService;
    private final GovernmentMinorApiService governmentMinorApiService;

    // =====================================================
    // PERSONAL DATA SERVICE (2 endpoints)
    // =====================================================

    /**
     * Personal Data Service - getData method
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_PersonalDataService/getData?pinfl={pinfl}&serial={serial}
     * </pre>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_PersonalDataService/getData?pinfl=12345678901234&serial=AA0000000' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "name_latin": "ISM",
     *   "surname_latin": "FAMILIYA",
     *   "patronym_latin": "OTASINING ISMI",
     *   "birth_date": "1996-01-01",
     *   "sex": "1",
     *   "document": "AA0000000",
     *   "citizenship": "ЎЗБЕКИСТОН",
     *   "nationality": "ҚОРАҚАЛПОҚ"
     * }
     * </pre>
     *
     * @param pinfl PINFL (14 digits)
     * @param serial Passport series (e.g., AA0000000)
     * @return personal data from government service
     */
    @GetMapping("/hemishe_PersonalDataService/getData")
    public ResponseEntity<Map<String, Object>> getPersonalData(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("serial") String serial
    ) {
        log.info("Personal data request - PINFL: {}, Serial: {}", pinfl, serial);
        Map<String, Object> data = personalDataService.getData(pinfl, serial);
        return ResponseEntity.ok(data);
    }

    /**
     * Personal Data Service - getPersonalData method (alternative)
     *
     * <p>Same as getData() but different method name for compatibility</p>
     *
     * @param pinfl PINFL
     * @param serial Passport series
     * @return personal data
     */
    @GetMapping("/hemishe_PersonalDataService/getPersonalData")
    public ResponseEntity<Map<String, Object>> getPersonalDataAlt(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("serial") String serial
    ) {
        log.info("Personal data request (alt) - PINFL: {}, Serial: {}", pinfl, serial);
        Map<String, Object> data = personalDataService.getPersonalData(pinfl, serial);
        return ResponseEntity.ok(data);
    }

    // =====================================================
    // PASSPORT DATA SERVICE (4 endpoints)
    // =====================================================

    /**
     * Get address data by PINFL
     * URL: /app/rest/v2/services/hemishe_PassportDataService/getAddress?pinfl={pinfl}
     */
    @GetMapping("/hemishe_PassportDataService/getAddress")
    public ResponseEntity<Map<String, Object>> getAddress(@RequestParam("pinfl") String pinfl) {
        log.info("Passport address request - PINFL: {}", pinfl);
        return ResponseEntity.ok(passportDataService.getAddress(pinfl));
    }

    /**
     * Get passport data by PINFL and birth date
     * URL: /app/rest/v2/services/hemishe_PassportDataService/getDataByPinflBirthdate
     */
    @GetMapping("/hemishe_PassportDataService/getDataByPinflBirthdate")
    public ResponseEntity<Map<String, Object>> getDataByPinflBirthdate(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("birthdate") String birthdate,
            @RequestParam("captchaId") String captchaId,
            @RequestParam("captchaValue") String captchaValue) {
        log.info("Passport data by PINFL/birthdate - PINFL: {}", pinfl);
        return ResponseEntity.ok(passportDataService.getDataByPinflBirthdate(pinfl, birthdate, captchaId, captchaValue));
    }

    /**
     * Get passport data by serial number and birth date
     * URL: /app/rest/v2/services/hemishe_PassportDataService/getDataBySNBirthdate
     */
    @GetMapping("/hemishe_PassportDataService/getDataBySNBirthdate")
    public ResponseEntity<Map<String, Object>> getDataBySNBirthdate(
            @RequestParam("seriaNumber") String seriaNumber,
            @RequestParam("birthdate") String birthdate,
            @RequestParam("captchaId") String captchaId,
            @RequestParam("captchaValue") String captchaValue) {
        log.info("Passport data by SN/birthdate - SN: {}", seriaNumber);
        return ResponseEntity.ok(passportDataService.getDataBySNBirthdate(seriaNumber, birthdate, captchaId, captchaValue));
    }

    /**
     * Get passport data by PINFL and serial number
     * URL: /app/rest/v2/services/hemishe_PassportDataService/getDataBySN
     */
    @GetMapping("/hemishe_PassportDataService/getDataBySN")
    public ResponseEntity<Map<String, Object>> getDataBySN(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("seriaNumber") String seriaNumber,
            @RequestParam("captchaId") String captchaId,
            @RequestParam("captchaValue") String captchaValue) {
        log.info("Passport data by PINFL/SN - PINFL: {}, SN: {}", pinfl, seriaNumber);
        return ResponseEntity.ok(passportDataService.getDataBySN(pinfl, seriaNumber, captchaId, captchaValue));
    }

    // =====================================================
    // BIMM SERVICE (5 endpoints)
    // =====================================================

    /**
     * Check disability status
     * URL: /app/rest/v2/services/hemishe_BimmService/disabilityCheck?pinfl={pinfl}&document={document}
     */
    @GetMapping("/hemishe_BimmService/disabilityCheck")
    public ResponseEntity<Map<String, Object>> disabilityCheck(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("document") String document) {
        log.info("BIMM disability check - PINFL: {}", pinfl);
        return ResponseEntity.ok(bimmService.disabilityCheck(pinfl, document));
    }

    /**
     * Check poverty register
     * URL: /app/rest/v2/services/hemishe_BimmService/provertyRegister?pinfl={pinfl}
     */
    @GetMapping("/hemishe_BimmService/provertyRegister")
    public ResponseEntity<Map<String, Object>> provertyRegister(@RequestParam("pinfl") String pinfl) {
        log.info("BIMM poverty register check - PINFL: {}", pinfl);
        return ResponseEntity.ok(bimmService.provertyRegister(pinfl));
    }

    /**
     * Get certificate info
     * URL: /app/rest/v2/services/hemishe_BimmService/certificate?pinfl={pinfl}
     */
    @GetMapping("/hemishe_BimmService/certificate")
    public ResponseEntity<Map<String, Object>> certificate(@RequestParam("pinfl") String pinfl) {
        log.info("BIMM certificate check - PINFL: {}", pinfl);
        return ResponseEntity.ok(bimmService.certificate(pinfl));
    }

    /**
     * Get academic degree info
     * URL: /app/rest/v2/services/hemishe_BimmService/academicDegree?pinfl={pinfl}
     */
    @GetMapping("/hemishe_BimmService/academicDegree")
    public ResponseEntity<Map<String, Object>> academicDegree(@RequestParam("pinfl") String pinfl) {
        log.info("BIMM academic degree check - PINFL: {}", pinfl);
        return ResponseEntity.ok(bimmService.academicDegree(pinfl));
    }

    /**
     * Get teacher training info
     * URL: /app/rest/v2/services/hemishe_BimmService/teacherTraining?pinfl={pinfl}
     */
    @GetMapping("/hemishe_BimmService/teacherTraining")
    public ResponseEntity<Map<String, Object>> teacherTraining(@RequestParam("pinfl") String pinfl) {
        log.info("BIMM teacher training check - PINFL: {}", pinfl);
        return ResponseEntity.ok(bimmService.teacherTraining(pinfl));
    }

    // =====================================================
    // SOCIAL SERVICE (6 endpoints)
    // =====================================================

    /**
     * Check single register
     * URL: /app/rest/v2/services/hemishe_SocialService/singleRegister?pinfl={pinfl}
     */
    @GetMapping("/hemishe_SocialService/singleRegister")
    public ResponseEntity<Map<String, Object>> singleRegister(@RequestParam("pinfl") String pinfl) {
        log.info("Social single register check - PINFL: {}", pinfl);
        return ResponseEntity.ok(socialService.singleRegister(pinfl));
    }

    /**
     * Get full daftar info
     * URL: /app/rest/v2/services/hemishe_SocialService/daftarFull?pinfl={pinfl}
     */
    @GetMapping("/hemishe_SocialService/daftarFull")
    public ResponseEntity<Map<String, Object>> daftarFull(@RequestParam("pinfl") String pinfl) {
        log.info("Social daftar full - PINFL: {}", pinfl);
        return ResponseEntity.ok(socialService.daftarFull(pinfl));
    }

    /**
     * Get short daftar info
     * URL: /app/rest/v2/services/hemishe_SocialService/daftarShort?pinfl={pinfl}
     */
    @GetMapping("/hemishe_SocialService/daftarShort")
    public ResponseEntity<Map<String, Object>> daftarShort(@RequestParam("pinfl") String pinfl) {
        log.info("Social daftar short - PINFL: {}", pinfl);
        return ResponseEntity.ok(socialService.daftarShort(pinfl));
    }

    /**
     * Check women registry
     * URL: /app/rest/v2/services/hemishe_SocialService/women?pinfl={pinfl}&sn={sn}
     */
    @GetMapping("/hemishe_SocialService/women")
    public ResponseEntity<Map<String, Object>> women(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("sn") String sn) {
        log.info("Social women registry check - PINFL: {}", pinfl);
        return ResponseEntity.ok(socialService.women(pinfl, sn));
    }

    /**
     * Check youth registry
     * URL: /app/rest/v2/services/hemishe_SocialService/young?pinfl={pinfl}&seria={seria}&number={number}
     */
    @GetMapping("/hemishe_SocialService/young")
    public ResponseEntity<Map<String, Object>> young(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("seria") String seria,
            @RequestParam("number") String number) {
        log.info("Social youth registry check - PINFL: {}", pinfl);
        return ResponseEntity.ok(socialService.young(pinfl, seria, number));
    }

    /**
     * Check VTEK status
     * URL: /app/rest/v2/services/hemishe_SocialService/vtek?pinfl={pinfl}&birthDate={birthDate}&birthDocument={birthDocument}
     */
    @GetMapping("/hemishe_SocialService/vtek")
    public ResponseEntity<Map<String, Object>> vtek(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("birthDate") String birthDate,
            @RequestParam("birthDocument") String birthDocument) {
        log.info("Social VTEK check - PINFL: {}", pinfl);
        return ResponseEntity.ok(socialService.vtek(pinfl, birthDate, birthDocument));
    }

    // =====================================================
    // GUVD SERVICE (2 endpoints)
    // =====================================================

    /**
     * Get GUVD classifiers
     * URL: /app/rest/v2/services/hemishe_GuvdService/classifiers
     */
    @GetMapping("/hemishe_GuvdService/classifiers")
    public ResponseEntity<Map<String, Object>> guvdClassifiers() {
        log.info("GUVD classifiers");
        return ResponseEntity.ok(governmentMinorApiService.guvdClassifiers());
    }

    /**
     * Get GUVD objects
     * URL: /app/rest/v2/services/hemishe_GuvdService/objects?type={type}&query={query}
     */
    @GetMapping("/hemishe_GuvdService/objects")
    public ResponseEntity<Map<String, Object>> guvdObjects(
            @RequestParam("type") String type,
            @RequestParam("query") String query) {
        log.info("GUVD objects - Type: {}, Query: {}", type, query);
        return ResponseEntity.ok(governmentMinorApiService.guvdObjects(type, query));
    }

    // =====================================================
    // TAX SERVICE (1 endpoint)
    // =====================================================

    /**
     * Check tax rent payment
     * URL: /app/rest/v2/services/hemishe_TaxService/rent?pinfl={pinfl}&period={period}
     */
    @GetMapping("/hemishe_TaxService/rent")
    public ResponseEntity<Map<String, Object>> taxRentCheck(
            @RequestParam("pinfl") String pinfl,
            @RequestParam("period") String period) {
        log.info("Tax rent check - PINFL: {}, Period: {}", pinfl, period);
        return ResponseEntity.ok(governmentMinorApiService.taxRentCheck(pinfl, period));
    }

    // =====================================================
    // ✅ 20 GOVERNMENT INTEGRATION ENDPOINTS
    // =====================================================
    // Total: 20/20 endpoints (100% complete)
    // - PersonalDataService: 2 endpoints
    // - PassportDataService: 4 endpoints
    // - BimmService: 5 endpoints
    // - SocialService: 6 endpoints
    // - GuvdService: 2 endpoints
    // - TaxService: 1 endpoint
    //
    // University services moved to feature modules:
    // - StudentApiController (18 endpoints)
    // - TeacherApiController (4 endpoints)
    // - ReferenceDataApiController (4 endpoints)
    // - ClassifierApiController (7 endpoints)
    // - DocumentApiController (6 endpoints)
    // - IntegrationApiController (6 endpoints)
    // - UtilityApiController (7 endpoints)
    // - MinorServicesApiController (10 endpoints)
    // - FinalServicesApiController (4 endpoints)
    // =====================================================
}
