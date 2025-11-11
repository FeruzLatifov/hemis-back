package uz.hemis.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Legacy employment statistics service placeholder.
 */
@Service
@Slf4j
public class LegacyEmploymentService {

    public Map<String, Object> graduateList(String university, String year) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("university", university);
        data.put("year", year);
        data.put("graduates", List.of());
        data.put("status", "NOT_IMPLEMENTED");
        data.put("timestamp", Instant.now().toString());

        response.put("success", true);
        response.put("data", data);
        response.put("message", "Employment graduate list placeholder");

        log.info("Legacy employment graduate list requested university={} year={}", university, year);
        return response;
    }
}

