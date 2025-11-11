package uz.hemis.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Legacy contract service stub that mirrors the structure of the old CUBA
 * <code>/services/contract/get</code> endpoint.
 */
@Service
@Slf4j
public class LegacyContractService {

    public Map<String, Object> fetch(String pinfl, String year) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (pinfl == null || pinfl.isBlank()) {
            response.put("success", false);
            response.put("message", "Parameter 'pinfl' is required");
            return response;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("year", year);
        data.put("status", "NOT_IMPLEMENTED");
        data.put("timestamp", Instant.now().toString());

        response.put("success", true);
        response.put("data", data);
        response.put("message", "Legacy contract integration placeholder");

        log.info("Legacy contract fetch requested pinfl={} year={}", pinfl, year);
        return response;
    }
}

