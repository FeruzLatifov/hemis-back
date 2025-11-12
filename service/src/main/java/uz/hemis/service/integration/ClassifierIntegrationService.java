package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassifierIntegrationService {
    public Map<String, List<?>> getAllItems() {
        return Map.of("country", List.of(), "region", List.of());
    }
    public List<String> getCategoryList() {
        return List.of("country", "region", "district", "gender", "citizenship");
    }
    public List<?> getSingleClassifier(String classifier) {
        return List.of();
    }
    public Map<String, Object> getHokimiyatClassifiers() {
        return Map.of("success", true, "data", List.of());
    }
}
