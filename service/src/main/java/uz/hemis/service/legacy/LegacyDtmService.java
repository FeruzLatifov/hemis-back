package uz.hemis.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Legacy stub for DTM mandate integration.
 *
 * <p>Replicates the response structure used by OLD-HEMIS while the real DTM
 * client is migrated.</p>
 */
@Service
@Slf4j
public class LegacyDtmService {

    public Map<String, Object> fetchMandat(String pinfl, String passport, String year) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (pinfl == null || pinfl.isBlank()) {
            response.put("success", false);
            response.put("message", "Parameter 'pinfl' is required");
            return response;
        }
        if (passport == null || passport.isBlank()) {
            response.put("success", false);
            response.put("message", "Parameter 'passport' is required");
            return response;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("passport", passport);
        data.put("year", year);
        data.put("status", "NOT_IMPLEMENTED");
        data.put("timestamp", Instant.now().toString());

        response.put("success", true);
        response.put("data", data);
        response.put("message", "DTM integration placeholder");

        log.info("Legacy DTM mandat stub invoked pinfl={} passport={}", pinfl, passport);
        return response;
    }
}

