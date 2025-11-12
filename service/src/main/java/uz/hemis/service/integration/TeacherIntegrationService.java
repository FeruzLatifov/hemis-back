package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherIntegrationService {
    public Map<String, Object> getTeacherId(TeacherIdRequest request) {
        log.info("Getting teacher ID for PINFL: {}", request.getPinfl());
        return Map.of("success", true, "teacherId", UUID.randomUUID());
    }
    public Map<String, Object> addJob(TeacherJobRequest request) {
        log.info("Adding job for teacher: {}", request.getEmployeeId());
        return Map.of("success", true, "jobId", UUID.randomUUID());
    }
}
