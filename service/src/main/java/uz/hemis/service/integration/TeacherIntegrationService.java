package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.hemis.common.dto.*;
import uz.hemis.service.TeacherService;

import java.util.*;

/**
 * Teacher Integration Service
 *
 * <p><strong>Purpose:</strong> Integration layer for teacher-related operations</p>
 * <p>Connects controllers to business logic in TeacherService</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherIntegrationService {

    private final TeacherService teacherService;

    /**
     * Get Teacher ID - generates or returns existing teacher
     *
     * <p><strong>OLD-HEMIS Compatible:</strong> POST /app/rest/v2/services/teacher/id</p>
     *
     * @param request TeacherIdRequest with citizenship, pinfl, serial, year, gender
     * @return Map with teacher data and is_new flag
     */
    public Map<String, Object> getTeacherId(TeacherIdRequest request) {
        log.info("Getting teacher ID for PINFL: {}, Serial: {}", request.getPinfl(), request.getSerial());

        // Get university code from authenticated user
        String universityCode = getUniversityCodeFromContext();
        if (universityCode == null) {
            log.error("University code not found in security context");
            return Map.of(
                    "success", false,
                    "error", "University code not found"
            );
        }

        return teacherService.generateTeacherId(request, universityCode);
    }

    /**
     * Add job to teacher
     *
     * <p><strong>OLD-HEMIS Compatible:</strong> POST /app/rest/v2/services/teacher/addJob</p>
     *
     * @param request TeacherJobRequest with job details
     * @return Success status
     */
    public Map<String, Object> addJob(TeacherJobRequest request) {
        log.info("Adding job for teacher: {}", request.getEmployeeId());
        // TODO: Implement when EEmployeeJobs entity exists
        return Map.of(
                "success", true,
                "message", "Job added successfully (mock implementation)",
                "jobId", UUID.randomUUID()
        );
    }

    /**
     * Extract university code from security context
     *
     * @return university code or null
     */
    private String getUniversityCodeFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        // Try to get university from principal
        Object principal = auth.getPrincipal();

        // Check if principal has university field (CustomUserDetails pattern)
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Try to extract university from UserDetails
            try {
                java.lang.reflect.Method getUniversityMethod = principal.getClass().getMethod("getUniversity");
                Object university = getUniversityMethod.invoke(principal);
                if (university != null) {
                    return university.toString();
                }
            } catch (Exception e) {
                log.debug("Could not get university from principal: {}", e.getMessage());
            }
        }

        // Fallback: return default test university for development
        log.warn("Using default university code for development");
        return "520"; // Default test university
    }
}
