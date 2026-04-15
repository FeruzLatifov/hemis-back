package uz.hemis.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Minimal legacy-compatible billing service used by the
 * {@link uz.hemis.app.controller.legacy.BillingLegacyController}.
 *
 * <p>This implementation keeps the historical API contract alive while the
 * real integration with the external billing platform is being redesigned.
 * Responses mirror the old structure: a {@code success} flag and either a
 * {@code data} payload or an explanatory message.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyBillingService {

    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> processScholarship(String tin, List<String> pinfls) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (tin == null || tin.isBlank()) {
            response.put("success", false);
            response.put("message", "Parameter 'tin' is required");
            return response;
        }

        response.put("success", true);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tin", tin);
        data.put("pinfls", pinfls == null ? List.of() : pinfls);
        data.put("requestId", UUID.randomUUID().toString());
        data.put("timestamp", Instant.now().toString());
        data.put("status", "QUEUED");

        response.put("data", data);
        response.put("message", "Billing scholarship integration placeholder");

        log.info("Legacy billing scholarship request queued for tin={} pinflCount={}", tin, data.get("pinfls") instanceof List ? ((List<?>) data.get("pinfls")).size() : 0);
        return response;
    }

    public Map<String, Object> fetchInvoice(Map<String, Object> params) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (params == null || params.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invoice params are required");
            return response;
        }

        response.put("success", true);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("params", params);
        data.put("invoices", List.of());
        data.put("status", "NOT_IMPLEMENTED");
        data.put("timestamp", Instant.now().toString());

        response.put("data", data);
        response.put("message", "Billing invoice integration placeholder");

        log.info("Legacy billing invoice request accepted params={}", params);
        return response;
    }

    /**
     * Lookup student fullname by PINFL from hemishe_e_student.
     * OLD-HEMIS format: "FAMILIYA ISM OTASI" (uppercase).
     *
     * @param pinfl student PINFL
     * @return uppercase fullname or empty string if not found
     */
    @Transactional(readOnly = true)
    public String lookupStudentFullname(String pinfl) {
        if (pinfl == null || pinfl.isBlank()) return "";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT first_name, lastname, fathername FROM hemishe_e_student WHERE pinfl = ? AND delete_ts IS NULL LIMIT 1",
                    pinfl);
            StringBuilder sb = new StringBuilder();
            String lastname = row.get("lastname") != null ? row.get("lastname").toString() : "";
            String firstName = row.get("first_name") != null ? row.get("first_name").toString() : "";
            String fathername = row.get("fathername") != null ? row.get("fathername").toString() : "";
            if (!lastname.isEmpty()) sb.append(lastname);
            if (!firstName.isEmpty()) { if (!sb.isEmpty()) sb.append(" "); sb.append(firstName); }
            if (!fathername.isEmpty()) { if (!sb.isEmpty()) sb.append(" "); sb.append(fathername); }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            log.debug("Student not found for PINFL: {}", pinfl);
            return "";
        }
    }
}

