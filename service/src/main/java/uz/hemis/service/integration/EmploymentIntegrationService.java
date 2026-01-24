package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.*;
import java.util.*;

/**
 * Employment Integration Service
 *
 * Mehnat vazirligi API bilan integratsiya
 *
 * OLD-HEMIS FORMAT BILAN 100% MOSLIK!
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmploymentIntegrationService {

    public Map<String, Object> submitGraduateList(GraduateListRequest request) {
        log.info("Submitting graduate list for year: {}", request.getYear());
        return Map.of("success", true, "submissionId", UUID.randomUUID());
    }

    /**
     * Mehnat daftarchasi ma'lumotlarini olish
     *
     * OLD-HEMIS response formati:
     * {
     *     "_entityName": "hemishe_Workbook",
     *     "id": "",
     *     "result": "1",
     *     "comments": "Ok",
     *     "data": {
     *         "_entityName": "hemishe_Data",
     *         "id": "",
     *         "jobs": [...]
     *     }
     * }
     */
    public Map<String, Object> getWorkbook(String pinfl) {
        log.info("Getting workbook for pinfl: {}", pinfl);

        // OLD-HEMIS formatiga mos response
        // TODO: Haqiqiy Mehnat vazirligi API dan ma'lumot olish
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("_entityName", "hemishe_Data");
        data.put("id", "");
        data.put("jobs", List.of()); // Haqiqiy integratsiyada to'ldiriladi

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", "hemishe_Workbook");
        response.put("id", "");
        response.put("result", "1");
        response.put("comments", "Ok");
        response.put("data", data);

        return response;
    }
}
