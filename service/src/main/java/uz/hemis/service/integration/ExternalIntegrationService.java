package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalIntegrationService {
    public Map<String, Object> getDtmMandat(String applicantId) {
        return Map.of("success", true, "mandat", Map.of("id", applicantId));
    }
    public Map<String, Object> getOakInfo(String pinfl) {
        return Map.of("success", true, "oakInfo", Map.of("pinfl", pinfl));
    }
    public Map<String, Object> getTaxRent(String pinfl) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("pinfl", pinfl);
        data.put("rentContracts", Collections.emptyList());
        data.put("message", "Tax rent data");

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }
    public Map<String, Object> getUzasboScholarship(String inn, Integer year, Integer month) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("inn", inn);
        data.put("year", year);
        data.put("month", month);
        data.put("scholarships", Collections.emptyList());
        data.put("message", "UzASBO scholarship data");

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }
}
