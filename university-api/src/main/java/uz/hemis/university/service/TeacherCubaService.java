package uz.hemis.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractInternalCubaService;
import uz.hemis.domain.entity.Teacher;
import uz.hemis.domain.repository.TeacherRepository;

import java.util.*;

/**
 * Teacher CUBA Service - OLD-HEMIS Compatibility
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Implements 4 CUBA service methods from rest-services.xml</li>
 *   <li>Different from TeacherService (CRUD) - these are CUBA-specific methods</li>
 *   <li>Used by universities calling old-HEMIS API patterns</li>
 * </ul>
 *
 * <p><strong>OPTIMIZATION:</strong></p>
 * <ul>
 *   <li>Extends AbstractInternalCubaService</li>
 *   <li>No code duplication (response builders, helpers in base class)</li>
 *   <li>Clean, focused business logic only</li>
 * </ul>
 *
 * <p><strong>Methods:</strong></p>
 * <ul>
 *   <li>id - Get teacher ID by some data</li>
 *   <li>getById - Get teacher by UUID</li>
 *   <li>get - Get teacher by PINFL</li>
 *   <li>addJob - Add job to teacher</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS URL Pattern:</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_TeacherService/get?pinfl={pinfl}
 * GET /app/rest/v2/services/hemishe_TeacherService/getById?id={id}
 * POST /app/rest/v2/services/hemishe_TeacherService/addJob
 * </pre>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherCubaService extends AbstractInternalCubaService {

    private final TeacherRepository teacherRepository;

    /**
     * Get teacher ID by data
     *
     * <p><strong>Method:</strong> id</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_TeacherService/id</p>
     *
     * <p>Note: This method's purpose is unclear from rest-services.xml</p>
     * <p>Parameter type is not specified, implementing as PINFL lookup</p>
     *
     * @param data Lookup data (PINFL or other identifier)
     * @return Teacher ID map
     */
    public Map<String, Object> id(String data) {
        log.info("Getting teacher ID - Data: {}", data);

        if (isEmpty(data)) {
            return errorResponse("invalid_parameter", "Data parameter required");
        }

        // Try to find teacher by PINFL first
        Optional<Teacher> teacher = teacherRepository.findByPinfl(data);

        if (teacher.isEmpty()) {
            return errorResponse("not_found", "Teacher not found");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", teacher.get().getId());
        result.put("pinfl", teacher.get().getPinfl());

        return result;
    }

    /**
     * Get teacher by UUID
     *
     * <p><strong>Method:</strong> getById</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_TeacherService/getById?id={id}</p>
     *
     * @param id Teacher UUID
     * @return Teacher data map or error
     */
    public Map<String, Object> getById(String id) {
        log.info("Getting teacher by ID - ID: {}", id);

        if (isEmpty(id)) {
            return errorResponse("invalid_parameter", "ID parameter required");
        }

        try {
            UUID uuid = UUID.fromString(id);
            Optional<Teacher> teacher = teacherRepository.findById(uuid);

            if (teacher.isEmpty()) {
                return notFoundResponse("Teacher");
            }

            return successResponse(teacherToMap(teacher.get()));

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID: {}", id);
            return errorResponse("invalid_id", "Invalid teacher ID format");
        }
    }

    /**
     * Get teacher by PINFL
     *
     * <p><strong>Method:</strong> get</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_TeacherService/get?pinfl={pinfl}</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return Teacher data map or error
     */
    public Map<String, Object> get(String pinfl) {
        log.info("Getting teacher by PINFL - PINFL: {}", pinfl);

        Map<String, Object> validationError = validateRequired("pinfl", pinfl);
        if (validationError != null) {
            return validationError;
        }

        Optional<Teacher> teacher = teacherRepository.findByPinfl(pinfl);

        if (teacher.isEmpty()) {
            return notFoundResponse("Teacher");
        }

        return successResponse(teacherToMap(teacher.get()));
    }

    /**
     * Add job to teacher
     *
     * <p><strong>Method:</strong> addJob</p>
     * <p><strong>URL:</strong> POST /app/rest/v2/services/hemishe_TeacherService/addJob</p>
     *
     * <p><strong>Request Body:</strong></p>
     * <pre>
     * {
     *   "teacher_id": "uuid",
     *   "university": "code",
     *   "position": "code",
     *   "staff_position": "code",
     *   "contract_number": "XXX",
     *   "contract_date": "2024-01-01",
     *   ...
     * }
     * </pre>
     *
     * <p><strong>Note:</strong> This requires EEmployeeJobs entity which doesn't exist yet</p>
     * <p>TODO: Implement when EEmployeeJobs entity is created</p>
     *
     * @param jobData Job data map
     * @return Success or error response
     */
    public Map<String, Object> addJob(Map<String, Object> jobData) {
        log.info("Adding job to teacher - Job data: {}", jobData);

        if (jobData == null || jobData.isEmpty()) {
            return errorResponse("invalid_parameter", "Job data required");
        }

        // Validate required fields
        String teacherId = (String) jobData.get("teacher_id");
        if (isEmpty(teacherId)) {
            return errorResponse("invalid_parameter", "teacher_id required");
        }

        // TODO: Implement actual job creation when EEmployeeJobs entity exists
        // For now, validate that teacher exists
        try {
            UUID uuid = UUID.fromString(teacherId);
            Optional<Teacher> teacher = teacherRepository.findById(uuid);

            if (teacher.isEmpty()) {
                return notFoundResponse("Teacher");
            }

            // TODO: Create and save EEmployeeJobs entity
            // EEmployeeJobs job = new EEmployeeJobs();
            // job.setTeacher(teacher.get());
            // job.setUniversity(jobData.get("university"));
            // ... set other fields
            // employeeJobsRepository.save(job);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Job added successfully (mock implementation)");
            result.put("teacher_id", teacherId);

            log.warn("addJob() is mock implementation - EEmployeeJobs entity not created yet");

            return result;

        } catch (IllegalArgumentException e) {
            log.error("Invalid teacher ID: {}", teacherId);
            return errorResponse("invalid_id", "Invalid teacher ID format");
        }
    }

    /**
     * Convert Teacher entity to Map for JSON response
     *
     * @param teacher Teacher entity
     * @return Teacher data map
     */
    private Map<String, Object> teacherToMap(Teacher teacher) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", teacher.getId());
        map.put("pinfl", teacher.getPinfl());
        map.put("first_name", teacher.getFirstName());
        map.put("second_name", teacher.getSecondName());
        map.put("third_name", teacher.getThirdName());
        map.put("first_name_latin", teacher.getFirstNameLatin());
        map.put("second_name_latin", teacher.getSecondNameLatin());
        map.put("third_name_latin", teacher.getThirdNameLatin());
        map.put("birth_date", teacher.getBirthDate());
        map.put("university", teacher.getUniversity());
        map.put("gender", teacher.getGender());
        map.put("citizenship", teacher.getCitizenship());
        return map;
    }
}
