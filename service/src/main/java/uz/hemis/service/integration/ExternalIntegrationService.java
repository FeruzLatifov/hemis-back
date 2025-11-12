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
    public Map<String, Object> getTaxRent(String contractNumber) {
        return Map.of("success", true, "rent", Map.of("contractNumber", contractNumber));
    }
    public Map<String, Object> getUzasboScholarship(String studentId, String period) {
        return Map.of("success", true, "scholarship", Map.of("studentId", studentId, "period", period));
    }
}
