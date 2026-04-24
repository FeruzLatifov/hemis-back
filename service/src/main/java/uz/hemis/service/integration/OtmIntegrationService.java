package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtmIntegrationService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get student info by student ID (string format from OLD-HEMIS).
     * Returns null if not found.
     */
    public Map<String, Object> getStudentInfoById(String studentId) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT s.id, s.pinfl, s.firstname, s.lastname, s.fathername, s.code, " +
                    "s._university, s.active " +
                    "FROM hemishe_e_student s " +
                    "WHERE s.code = ? AND s.delete_ts IS NULL " +
                    "LIMIT 1",
                    studentId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", row.get("code"));
            result.put("pinfl", row.get("pinfl"));
            result.put("name", buildFullName(row));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Get student info by PINFL for a specific university.
     * Returns null if not found in the specified university.
     */
    public Map<String, Object> getStudentInfoByPinfl(String pinfl, String universityCode) {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT s.id, s.pinfl, s.firstname, s.lastname, s.fathername, s.code, " +
                    "s._university, s.active " +
                    "FROM hemishe_e_student s " +
                    "WHERE s.pinfl = ? AND s._university = ? AND s.delete_ts IS NULL " +
                    "LIMIT 1",
                    pinfl, universityCode);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("pinfl", row.get("pinfl"));
            result.put("name", buildFullName(row));
            result.put("university", row.get("_university"));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Get student list by tutor using university code and tutor PINFL (OLD-HEMIS format).
     */
    public List<Map<String, Object>> getStudentListByTutor(String university, String tutorPinfl) {
        // Stub — tutor mapping not yet implemented
        return List.of();
    }

    private String buildFullName(Map<String, Object> row) {
        StringBuilder sb = new StringBuilder();
        Object lastname = row.get("lastname");
        Object firstname = row.get("firstname");
        Object fathername = row.get("fathername");
        if (lastname != null) sb.append(lastname);
        if (firstname != null) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(firstname);
        }
        if (fathername != null) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(fathername);
        }
        return sb.toString();
    }
}
