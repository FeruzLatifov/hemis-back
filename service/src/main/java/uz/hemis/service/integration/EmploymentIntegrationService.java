package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmploymentIntegrationService {
    public Map<String, Object> submitGraduateList(GraduateListRequest request) {
        log.info("Submitting graduate list for year: {}", request.getYear());
        return Map.of("success", true, "submissionId", UUID.randomUUID());
    }
    public Map<String, Object> getWorkbook(String pinfl) {
        return Map.of("success", true, "workbook", Map.of("pinfl", pinfl, "jobs", List.of()));
    }
}
