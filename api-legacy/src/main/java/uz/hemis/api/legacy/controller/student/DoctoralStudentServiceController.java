package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.log.LogSafe;
import uz.hemis.domain.entity.student.DoctoralStudent;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.service.legacy.ReferenceDataLegacyService;
import uz.hemis.service.student.DoctoralStudentService;

import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Doctoral Student Service Controller - OLD-HEMIS Compatible
 *
 * <p>Doktorant ID yaratish xizmati</p>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>POST /app/rest/v2/services/doctoral-student/id - Doktorant ID olish/yaratish</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "07.Doktorant", description = "Doktorant bilan bog'liq operatsiyalar")
@RestController
@RequestMapping("/app/rest/v2/services/doctoral-student")
@RequiredArgsConstructor
@Slf4j
public class DoctoralStudentServiceController {

    private final DoctoralStudentService doctoralStudentService;
    private final LegacySecurityHelper securityHelper;
    private final ReferenceDataLegacyService referenceDataService;

    /**
     * Doktorant ID yaratish yoki olish
     *
     * <p>Old-hemis: DoctoralStudentServiceBean.id()</p>
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/id")
    @Operation(
            summary = "Doktorant ID olish",
            description = """
                Doktorant Universal ID sini olish yoki yangi yaratish.

                **Endpoint:** POST /app/rest/v2/services/doctoral-student/id
                **Auth:** Bearer token (required)

                **Logic:**
                1. O'zbekiston fuqarosi (citizenship=11): PINFL bo'yicha qidiradi
                2. Chet el fuqarosi: passport serial bo'yicha qidiradi
                3. Topilsa: mavjud doktorant qaytariladi (is_new=false)
                4. Topilmasa: yangi doktorant yaratiladi (is_new=true)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> id(@RequestBody Map<String, Object> request) {
        log.info("[CUBA Service] doctoral-student/id: request={}", request);

        Object dataObj = request.get("data");
        Map<String, Object> data;
        if (dataObj instanceof Map) {
            data = (Map<String, Object>) dataObj;
        } else {
            data = request;
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // Validate
        String citizenship = safeString(data.get("citizenship"));
        String pinfl = safeString(data.get("pinfl"));
        String serial = safeString(data.get("serial"));
        String year = safeString(data.get("year"));
        String educationType = safeString(data.get("education_type"));

        if (citizenship == null || citizenship.isEmpty()) {
            result.put("success", false);
            result.put("message", "Citizenship value incorrect");
            result.put("data", buildInputData(data));
            return ResponseEntity.ok(result);
        }

        // Validate citizenship against DB classifier (old-hemis compatible)
        if (!isValidClassifier("hemishe_h_citizenship", citizenship)) {
            result.put("success", false);
            result.put("message", "Citizenship value not available!");
            result.put("data", buildInputData(data));
            return ResponseEntity.ok(result);
        }

        // Validate education_type against DB classifier
        if (educationType != null && !educationType.isEmpty()
                && !isValidClassifier("hemishe_h_education_type", educationType)) {
            result.put("success", false);
            result.put("message", "Education type value incorrect");
            result.put("data", buildInputData(data));
            return ResponseEntity.ok(result);
        }

        if ("11".equals(citizenship) && (pinfl == null || pinfl.isEmpty())) {
            result.put("success", false);
            result.put("message", "PINFL value incorrect");
            return ResponseEntity.ok(result);
        }

        if (serial == null || serial.isEmpty()) {
            result.put("success", false);
            result.put("message", "Passport serial value incorrect");
            return ResponseEntity.ok(result);
        }

        // Search existing - service orqali
        try {
            Optional<DoctoralStudent> existing;
            if ("11".equals(citizenship)) {
                existing = doctoralStudentService.findEntityByPassportPin(pinfl);
            } else {
                existing = doctoralStudentService.findEntityByPassportNumber(serial);
            }

            // Fetch personal data for Uzbek citizens (old-hemis compatible)
            Map<String, Object> personalData = null;
            if ("11".equals(citizenship)) {
                personalData = fetchPersonalData(pinfl, serial);
            }

            if (existing.isPresent()) {
                DoctoralStudent student = existing.get();
                log.info("Found existing doctoral student: {}", student.getStudentIdNumber());
                result.put("success", true);
                result.put("is_new", false);
                result.put("unique_id", student.getStudentIdNumber());
                result.put("student", doctoralStudentService.toStudentMap(student));
                result.put("personal_data", personalData);
                return ResponseEntity.ok(result);
            }

            // Create new
            String universityCode = securityHelper.getUniversityCodeFromContext();
            if (universityCode == null) {
                result.put("success", false);
                result.put("message", "User university not configured");
                return ResponseEntity.ok(result);
            }

            String yearSuffix = (year != null && year.length() >= 2)
                    ? year.substring(year.length() - 2) : (year != null ? year : "00");
            String eduType = (educationType != null && !educationType.isEmpty()) ? educationType : "00";

            // ID format: universityCode + yearSuffix + educationType + sequence
            String codePrefix = universityCode + yearSuffix + eduType;

            long count = doctoralStudentService.count();
            String sequence = String.format("%05d", count + 1);
            String uniqueId = codePrefix + sequence;

            DoctoralStudent student = new DoctoralStudent();
            student.setId(UUID.randomUUID());
            student.setPassportPin(pinfl);
            student.setPassportNumber(serial);
            student.setStudentIdNumber(uniqueId);
            student.setUniversity(universityCode);
            student.setCitizenship(citizenship);
            student.setActive(true);

            DoctoralStudent saved = doctoralStudentService.saveEntity(student);
            log.info("Created new doctoral student: {}", saved.getStudentIdNumber());

            result.put("success", true);
            result.put("is_new", true);
            result.put("unique_id", saved.getStudentIdNumber());
            result.put("student", doctoralStudentService.toStudentMap(saved));
            result.put("personal_data", personalData);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in doctoral-student/id: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    private boolean isValidClassifier(String tableName, String code) {
        return referenceDataService.isValidClassifier(tableName, code);
    }

    private Map<String, Object> buildInputData(Map<String, Object> data) {
        LinkedHashMap<String, Object> inputData = new LinkedHashMap<>();
        inputData.put("citizenship", safeString(data.get("citizenship")));
        inputData.put("pinfl", safeString(data.get("pinfl")));
        inputData.put("serial", safeString(data.get("serial")));
        inputData.put("year", safeString(data.get("year")));
        inputData.put("education_type", safeString(data.get("education_type")));
        return inputData;
    }

    /**
     * Fetch personal data from MVD via talaba.edu.uz (old-hemis compatible).
     */
    private Map<String, Object> fetchPersonalData(String pinfl, String serial) {
        try {
            String url = "https://talaba.edu.uz/api/my_edu_uz/student_mvd_hemis.php?TOKEN=12345&pinfl="
                    + pinfl + "&p_seriya=" + (serial != null ? serial : "");

            javax.net.ssl.HttpsURLConnection conn = (javax.net.ssl.HttpsURLConnection)
                    java.net.URI.create(url).toURL().openConnection();
            javax.net.ssl.SSLContext sc = uz.hemis.service.base.AbstractGovernmentApiService.getGovSslContextStatic();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier((h, s) -> true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.connect();

            String json = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            conn.disconnect();

            if (json.replaceAll("[^a-zA-Z0-9]", "").equalsIgnoreCase("no") || json.isBlank()) {
                return null;
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = mapper.readValue(json, Map.class);
            data.put("success", true);
            return data;
        } catch (Exception e) {
            log.warn("Failed to fetch personal data for PINFL {}: {}", LogSafe.pinfl(pinfl), e.getMessage());
            return null;
        }
    }

    private String safeString(Object obj) {
        if (obj == null) return null;
        String str = String.valueOf(obj);
        return "null".equals(str) ? null : str;
    }
}
