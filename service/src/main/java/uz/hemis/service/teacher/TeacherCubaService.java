package uz.hemis.service.teacher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.base.CubaResponseHelper;
import uz.hemis.domain.entity.employee.Employee;
import uz.hemis.domain.entity.employee.EmployeeJobs;
import uz.hemis.domain.entity.employee.Teacher;
import uz.hemis.domain.repository.EmployeeJobsRepository;
import uz.hemis.domain.repository.TeacherRepository;

import java.time.LocalDate;
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
public class TeacherCubaService {

    private final TeacherRepository teacherRepository;
    private final EmployeeJobsRepository employeeJobsRepository;

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

        if (CubaResponseHelper.isEmpty(data)) {
            return CubaResponseHelper.errorResponse("invalid_parameter", "Data parameter required");
        }

        // Try to find teacher by PINFL first
        Optional<Teacher> teacher = teacherRepository.findByPinfl(data);

        if (teacher.isEmpty()) {
            return CubaResponseHelper.errorResponse("not_found", "Teacher not found");
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

        if (CubaResponseHelper.isEmpty(id)) {
            return CubaResponseHelper.errorResponse("invalid_parameter", "ID parameter required");
        }

        try {
            UUID uuid = UUID.fromString(id);
            Optional<Teacher> teacher = teacherRepository.findById(uuid);

            if (teacher.isEmpty()) {
                return CubaResponseHelper.notFoundResponse("Teacher");
            }

            return CubaResponseHelper.successResponse(teacherToMap(teacher.get()));

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID: {}", id);
            return CubaResponseHelper.errorResponse("invalid_id", "Invalid teacher ID format");
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

        Map<String, Object> validationError = CubaResponseHelper.validateRequired("pinfl", pinfl);
        if (validationError != null) {
            return validationError;
        }

        Optional<Teacher> teacher = teacherRepository.findByPinfl(pinfl);

        if (teacher.isEmpty()) {
            return CubaResponseHelper.notFoundResponse("Teacher");
        }

        return CubaResponseHelper.successResponse(teacherToMap(teacher.get()));
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
     * <p>DEFERRED: Implement when EEmployeeJobs entity is created</p>
     *
     * @param jobData Job data map
     * @return Success or error response
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> addJob(Map<String, Object> requestBody) {
        log.info("Adding job to teacher - request: {}", requestBody);

        if (requestBody == null || requestBody.isEmpty()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "Request body is empty");
            return error;
        }

        // Extract job data from wrapper
        Map<String, Object> job = (Map<String, Object>) requestBody.get("job");
        if (job == null) {
            job = requestBody;
        }

        // Extract employee UUID from job.employee.id or job.employee (string)
        UUID employeeId = extractUuid(job.get("employee"));
        if (employeeId == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "employee required");
            return error;
        }

        // Create EmployeeJobs entity — field names match EmployeeJobs.java (V009 schema)
        EmployeeJobs entity = new EmployeeJobs();
        Employee employee = new Employee();
        employee.setId(employeeId);
        entity.setEmployee(employee);
        entity.setUniversityCode(extractCode(job.get("university")));
        entity.setDepartmentCode(extractCode(job.get("department")));
        entity.setEmployeeType(extractCode(job.get("employeeType")));
        entity.setPositionCode(extractCode(job.get("employeePosition")));
        entity.setEmployeeRate(extractCode(job.get("employeeRate")));
        entity.setEmploymentForm(extractCode(job.get("employeeForm")));
        // employee_status is derived from is_current + end_date — not a separate column anymore.
        entity.setIsCurrent(job.get("jobEndDate") == null);
        entity.setStartDate(parseDate(job.get("jobStartDate")));
        entity.setEndDate(parseDate(job.get("jobEndDate")));
        entity.setContractDate(parseDate(job.get("contractDate")));
        entity.setContractNumber(job.get("contractNumber") != null ? job.get("contractNumber").toString() : null);
        entity.setDecreeDate(parseDate(job.get("decreeDate")));
        entity.setDecreeNumber(job.get("decreeNumber") != null ? job.get("decreeNumber").toString() : null);

        try {
            EmployeeJobs saved = employeeJobsRepository.saveAndFlush(entity);
            log.info("Job created: id={}, employee={}", saved.getId(), employeeId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "Lavozim muvaffaqiyatli qo'shildi");
            result.put("id", saved.getId().toString());
            return result;

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Constraint violation on addJob: {}", e.getMessage());
            // Old-hemis format: {success: false, message: "Xodimda bunday lavozim mavjud", data: error_details}
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("success", false);
            error.put("message", "Xodimda bunday lavozim mavjud");
            error.put("data", e.getMostSpecificCause().getMessage());
            return error;
        }
    }

    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s.isEmpty() ? null : UUID.fromString(s);
        if (value instanceof Map) {
            Object id = ((Map<String, Object>) value).get("id");
            if (id == null) return null;
            return id instanceof String s ? (s.isEmpty() ? null : UUID.fromString(s)) : null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s.isEmpty() ? null : s;
        if (value instanceof Map) {
            Object code = ((Map<String, Object>) value).get("code");
            return code != null ? code.toString() : null;
        }
        return value.toString();
    }

    private LocalDate parseDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        String str = value.toString();
        return str.isEmpty() ? null : LocalDate.parse(str);
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
        map.put("birth_date", teacher.getBirthDate());
        map.put("university", teacher.getUniversity());
        map.put("gender", teacher.getGender());
        map.put("citizenship", teacher.getCitizenship());
        return map;
    }
}
