package uz.hemis.api.legacy.controller.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Diplom-Blank Service Controller - CUBA REST API Compatible
 *
 * <p><strong>URL Pattern:</strong> {@code /app/rest/v2/services/diplom-blank/*}</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping({"/app/rest/v2/services/diplom-blank", "/services/diplom-blank"})
@Tag(name = "Diplom-Blank Service API", description = "CUBA compatible diploma blank service endpoints")
@RequiredArgsConstructor
@Slf4j
public class DiplomBlankServiceController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get diploma blanks by university and year
     */
    @GetMapping("/get")
    @Operation(summary = "Get diploma blanks", description = "Returns diploma blanks for given university and year")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> get(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String university,
            @Parameter(description = "Academic year", required = true, example = "2024")
            @RequestParam Integer year) {
        log.info("[CUBA Service] diplom-blank/get: university={}, year={}", university, year);

        try {
            String sql = """
                SELECT b.id, b.version, b.blank_number, b.blank_seria, b.cancel_reason,
                       b._blank_status, b._blank_year, b._education_type, b._university,
                       b._blank_category, b.blank_generate_status_code
                FROM hemishe_e_diplom_blank b
                WHERE b._university = ? AND b._blank_year = CAST(? AS varchar)
                  AND b.delete_ts IS NULL
                """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, university, String.valueOf(year));

            List<Map<String, Object>> blanks = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                LinkedHashMap<String, Object> blank = new LinkedHashMap<>();
                blank.put("_entityName", "hemishe_EDiplomBlank");
                blank.put("id", str(row.get("id")));

                // blankGenerateStatus
                String bgsCode = str(row.get("blank_generate_status_code"));
                if (bgsCode != null) {
                    blank.put("blankGenerateStatus", fetchClassifier(
                            "hemishe_h_diplom_blank_generate_status",
                            "hemishe_HDiplomBlankGenerateStatus", bgsCode));
                }

                // blankYear
                String yearCode = str(row.get("_blank_year"));
                if (yearCode != null) {
                    blank.put("blankYear", fetchClassifier(
                            "hemishe_h_education_year",
                            "hemishe_HEducationYear", yearCode));
                }

                // blankStatus
                String statusCode = str(row.get("_blank_status"));
                if (statusCode != null) {
                    blank.put("blankStatus", fetchClassifier(
                            "hemishe_h_diplom_blank_status",
                            "hemishe_HDiplomBlankStatus", statusCode));
                }

                // educationType (has name_en)
                String eduTypeCode = str(row.get("_education_type"));
                if (eduTypeCode != null) {
                    blank.put("educationType", fetchEducationType(eduTypeCode));
                }

                // university
                String uniCode = str(row.get("_university"));
                if (uniCode != null) {
                    blank.put("university", fetchUniversity(uniCode));
                }

                blank.put("version", row.get("version"));
                blank.put("blankNumber", str(row.get("blank_number")));
                blank.put("blankSeria", str(row.get("blank_seria")));

                // blankCategory
                String catCode = str(row.get("_blank_category"));
                if (catCode != null) {
                    blank.put("blankCategory", fetchClassifier(
                            "hemishe_h_diplom_blank_category",
                            "hemishe_HDiplomBlankCategory", catCode));
                }

                blanks.add(blank);
            }

            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Ok");
            response.put("data", blanks);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in diplom-blank/get: {}", e.getMessage(), e);
            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Set diploma blank status
     */
    @GetMapping("/setStatus")
    @Operation(summary = "Set diploma blank status", description = "Updates status of diploma blank")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> setStatus(
            @Parameter(description = "Diploma blank code", required = true)
            @RequestParam String blankCode,
            @Parameter(description = "Status code", required = true)
            @RequestParam String statusCode,
            @Parameter(description = "Reason for status change", required = false)
            @RequestParam(required = false) String reason) {
        log.info("[CUBA Service] diplom-blank/setStatus: blankCode={}, statusCode={}, reason={}",
                blankCode, statusCode, reason);

        try {
            // Check if blank exists (search by blank_number or blank_seria+blank_number)
            String sql = """
                SELECT id FROM hemishe_e_diplom_blank
                WHERE (blank_number = ? OR CONCAT(blank_seria, blank_number) = ?)
                  AND delete_ts IS NULL
                LIMIT 1
                """;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, blankCode, blankCode);

            if (rows.isEmpty()) {
                LinkedHashMap<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("message", "Diplom blank not found!");
                return ResponseEntity.ok(response);
            }

            // Update status
            String updateSql = """
                UPDATE hemishe_e_diplom_blank
                SET _blank_status = ?, cancel_reason = ?, update_ts = NOW()
                WHERE id = ?::uuid
                """;
            String id = str(rows.get(0).get("id"));
            jdbcTemplate.update(updateSql, statusCode, reason, id);

            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Diplom blank status updated!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in diplom-blank/setStatus: {}", e.getMessage(), e);
            LinkedHashMap<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    private Map<String, Object> fetchClassifier(String tableName, String entityName, String code) {
        try {
            String sql = "SELECT code, name, version FROM " + tableName
                    + " WHERE code = ? AND delete_ts IS NULL LIMIT 1";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, code);
            if (rows.isEmpty()) return null;
            Map<String, Object> row = rows.get(0);
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", entityName);
            result.put("id", str(row.get("code")));
            result.put("code", str(row.get("code")));
            result.put("name", str(row.get("name")));
            result.put("version", row.get("version"));
            return result;
        } catch (Exception e) {
            log.debug("Could not fetch classifier {}/{}: {}", tableName, code, e.getMessage());
            return null;
        }
    }

    private Map<String, Object> fetchEducationType(String code) {
        try {
            String sql = "SELECT code, name, name_en, version FROM hemishe_h_education_type WHERE code = ? AND delete_ts IS NULL LIMIT 1";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, code);
            if (rows.isEmpty()) return null;
            Map<String, Object> row = rows.get(0);
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", "hemishe_HEducationType");
            result.put("id", str(row.get("code")));
            result.put("code", str(row.get("code")));
            result.put("name", str(row.get("name")));
            result.put("nameEn", str(row.get("name_en")));
            result.put("version", row.get("version"));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> fetchUniversity(String code) {
        try {
            String sql = "SELECT code, name, version FROM hemishe_e_university WHERE code = ? AND delete_ts IS NULL LIMIT 1";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, code);
            if (rows.isEmpty()) return null;
            Map<String, Object> row = rows.get(0);
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", "hemishe_EUniversity");
            result.put("id", str(row.get("code")));
            result.put("code", str(row.get("code")));
            result.put("version", row.get("version"));
            result.put("name", str(row.get("name")));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private String str(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
