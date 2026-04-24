package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Legacy tashqi integratsiya xizmatlari (Tax, UzAsbo).
 * ExternalIntegrationController (api-legacy) orqali chaqiriladi.
 *
 * <p>Qaytariladigan JSON old-hemis formatiga mos bo'lishi kerak —
 * univer tomondan backward compat saqlash uchun (rules.md #1).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalIntegrationService {

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
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", Collections.emptyList());
        return result;
    }
}
