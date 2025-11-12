package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtmIntegrationService {
    public Map<String, Object> getStudentInfoById(UUID studentId) {
        return Map.of("success", true, "student", Map.of("id", studentId, "name", "..."));
    }
    public Map<String, Object> getStudentInfoByPinfl(String pinfl) {
        return Map.of("success", true, "student", Map.of("pinfl", pinfl, "name", "..."));
    }
    public List<Map<String, Object>> getStudentListByTutor(UUID tutorId) {
        return List.of(Map.of("id", UUID.randomUUID(), "name", "..."));
    }
}
