package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractIntegrationService {
    public Map<String, Object> getContract(UUID studentId) {
        return Map.of("success", true, "contract", Map.of("id", UUID.randomUUID(), "studentId", studentId));
    }
}
