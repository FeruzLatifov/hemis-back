package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for diploma blank operations via native SQL.
 *
 * Extracted from DiplomBlankServiceController to move JdbcTemplate usage
 * out of the controller layer.
 *
 * @since 1.5.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiplomBlankLegacyService {

    private final JdbcTemplate jdbcTemplate;
    private final CubaNestedObjectLoader nestedObjectLoader;

    /**
     * Get diploma blanks by university code and year.
     *
     * @param university university code
     * @param year academic year
     * @return list of diploma blank maps in CUBA format
     */
    public List<Map<String, Object>> getDiplomBlanks(String university, Integer year) {
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
                blank.put("blankGenerateStatus", nestedObjectLoader.loadClassifier(
                        "hemishe_h_diplom_blank_generate_status",
                        "HDiplomBlankGenerateStatus", bgsCode));
            }

            // blankYear
            String yearCode = str(row.get("_blank_year"));
            if (yearCode != null) {
                blank.put("blankYear", nestedObjectLoader.loadClassifier(
                        "hemishe_h_education_year",
                        "HEducationYear", yearCode));
            }

            // blankStatus
            String statusCode = str(row.get("_blank_status"));
            if (statusCode != null) {
                blank.put("blankStatus", nestedObjectLoader.loadClassifier(
                        "hemishe_h_diplom_blank_status",
                        "HDiplomBlankStatus", statusCode));
            }

            // educationType (has name_en)
            String eduTypeCode = str(row.get("_education_type"));
            if (eduTypeCode != null) {
                blank.put("educationType", nestedObjectLoader.loadClassifierWithNames(
                        "hemishe_h_education_type",
                        "HEducationType", eduTypeCode));
            }

            // university
            String uniCode = str(row.get("_university"));
            if (uniCode != null) {
                blank.put("university", nestedObjectLoader.loadUniversity(uniCode));
            }

            blank.put("version", row.get("version"));
            blank.put("blankNumber", str(row.get("blank_number")));
            blank.put("blankSeria", str(row.get("blank_seria")));

            // blankCategory
            String catCode = str(row.get("_blank_category"));
            if (catCode != null) {
                blank.put("blankCategory", nestedObjectLoader.loadClassifier(
                        "hemishe_h_diplom_blank_category",
                        "HDiplomBlankCategory", catCode));
            }

            blanks.add(blank);
        }

        return blanks;
    }

    /**
     * Find diploma blank ID by blank code (blank_number or seria+number).
     *
     * @param blankCode the blank code to search
     * @return the UUID string of the found blank, or null if not found
     */
    public String findDiplomBlankId(String blankCode) {
        String sql = """
            SELECT id FROM hemishe_e_diplom_blank
            WHERE (blank_number = ? OR CONCAT(blank_seria, blank_number) = ?)
              AND delete_ts IS NULL
            LIMIT 1
            """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, blankCode, blankCode);

        if (rows.isEmpty()) {
            return null;
        }
        return str(rows.get(0).get("id"));
    }

    /**
     * Update diploma blank status.
     *
     * @param id the blank UUID string
     * @param statusCode the new status code
     * @param reason optional reason for the status change
     */
    public void updateDiplomBlankStatus(String id, String statusCode, String reason) {
        String updateSql = """
            UPDATE hemishe_e_diplom_blank
            SET _blank_status = ?, cancel_reason = ?, update_ts = NOW()
            WHERE id = ?::uuid
            """;
        jdbcTemplate.update(updateSql, statusCode, reason, id);
    }

    private String str(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
