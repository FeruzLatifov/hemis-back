package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.DoctoralStudent;
import uz.hemis.domain.repository.DoctoralStudentRepository;
import uz.hemis.domain.repository.UserRepository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

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

    private final DoctoralStudentRepository doctoralStudentRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Doktorant ID yaratish yoki olish
     *
     * <p>Old-hemis: DoctoralStudentServiceBean.id()</p>
     */
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
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "success": true,
                                  "is_new": false,
                                  "unique_id": "4012411001",
                                  "student": {"id": "uuid", "studentIdNumber": "4012411001"}
                                }
                                """))),
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

        // Search existing - mavjud Repository metodlari
        try {
        Optional<DoctoralStudent> existing;
        if ("11".equals(citizenship)) {
            existing = doctoralStudentRepository.findByPassportPin(pinfl);
        } else {
            existing = doctoralStudentRepository.findByPassportNumber(serial);
        }

        if (existing.isPresent()) {
            DoctoralStudent student = existing.get();
            log.info("Found existing doctoral student: {}", student.getStudentIdNumber());
            result.put("success", true);
            result.put("is_new", false);
            result.put("unique_id", student.getStudentIdNumber());
            result.put("student", studentToMap(student));
            return ResponseEntity.ok(result);
        }

        // Create new
        {
            String universityCode = getUniversityCode();
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

            // JPA Example bilan count olish (mavjud Repository metodi emas, lekin JPA standart)
            long count = doctoralStudentRepository.count(); // Oddiy count
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

            DoctoralStudent saved = doctoralStudentRepository.save(student);
            log.info("Created new doctoral student: {}", saved.getStudentIdNumber());

            result.put("success", true);
            result.put("is_new", true);
            result.put("unique_id", saved.getStudentIdNumber());
            result.put("student", studentToMap(saved));
            return ResponseEntity.ok(result);

        }
        } catch (Exception e) {
            log.error("Error in doctoral-student/id: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    private Map<String, Object> studentToMap(DoctoralStudent s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId().toString());
        map.put("studentIdNumber", s.getStudentIdNumber());
        map.put("passportPin", s.getPassportPin());
        map.put("passportNumber", s.getPassportNumber());
        map.put("firstName", s.getFirstName());
        map.put("secondName", s.getSecondName());
        map.put("thirdName", s.getThirdName());
        return map;
    }

    private String getUniversityCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            try {
                UUID userId = UUID.fromString(jwt.getSubject());
                return userRepository.findById(userId)
                        .map(u -> u.getUniversity() != null ? u.getUniversity().getCode() : null)
                        .orElse(null);
            } catch (Exception e) {
                log.debug("Could not get university: {}", e.getMessage());
            }
        }
        return null;
    }

    private boolean isValidClassifier(String tableName, String code) {
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE code = ? AND delete_ts IS NULL";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code);
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("Error checking classifier {}/{}: {}", tableName, code, e.getMessage());
            return false;
        }
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

    private String safeString(Object obj) {
        if (obj == null) return null;
        String str = String.valueOf(obj);
        return "null".equals(str) ? null : str;
    }
}
