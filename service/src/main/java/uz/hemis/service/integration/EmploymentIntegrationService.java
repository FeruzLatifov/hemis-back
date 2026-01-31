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
     * OLD-HEMIS response formati (ma'lumot topilmagan holat):
     * {
     *     "_entityName": "hemishe_Workbook",
     *     "id": "",
     *     "result": "4",
     *     "comments": "Данные не найдены"
     * }
     *
     * OLD-HEMIS response formati (ma'lumot topilgan holat):
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

        // TODO: Haqiqiy Mehnat vazirligi API dan ma'lumot olish
        // Hozircha ma'lumot topilmagan holat qaytariladi (old-hemis bilan mos)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", "hemishe_Workbook");
        response.put("id", "");
        response.put("result", "4");
        response.put("comments", "Данные не найдены");
        // result="4" holatda "data" field YO'Q (old-hemis bilan mos)

        return response;
    }
}
