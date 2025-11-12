package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationIntegrationService {
    public Map<String, Object> getAllTranslations() {
        return Map.of("success", true, "translations", List.of());
    }
    public Map<String, Object> getTranslationsFiltered(TranslationFilterRequest request) {
        return Map.of("success", true, "translations", List.of());
    }
    public Map<String, Object> getTranscript(UUID applicationId) {
        return Map.of("success", true, "transcript", Map.of("id", applicationId));
    }
}
