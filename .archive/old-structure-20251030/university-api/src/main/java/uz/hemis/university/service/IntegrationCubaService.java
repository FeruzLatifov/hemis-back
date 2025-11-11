package uz.hemis.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.hemis.app.service.base.AbstractInternalCubaService;
import uz.hemis.domain.repository.StudentRepository;

import java.util.*;

/**
 * Integration CUBA Services - External System Integrations
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Implements integration services for external systems</li>
 *   <li>Employment, OTM (Other universities) services</li>
 *   <li>Used for data exchange with government and partner systems</li>
 * </ul>
 *
 * <p><strong>OPTIMIZATION:</strong></p>
 * <ul>
 *   <li>Extends AbstractInternalCubaService</li>
 *   <li>Multiple integration services in ONE class</li>
 *   <li>No code duplication</li>
 * </ul>
 *
 * <p><strong>Services (2 services, 6 methods):</strong></p>
 * <ul>
 *   <li>Employment Service - 3 methods (workbook, graduate, graduateList)</li>
 *   <li>OTM Service - 3 methods (studentListByTutor, studentInfoById, studentInfoByPinfl)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationCubaService extends AbstractInternalCubaService {

    private final StudentRepository studentRepository;

    // TODO: Inject EmploymentRepository when created
    // private final EmploymentRepository employmentRepository;

    // =====================================================
    // EMPLOYMENT SERVICE (3 methods)
    // =====================================================

    /**
     * Get employment workbook data by PINFL
     *
     * <p><strong>Method:</strong> workbook</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_EmploymentService/workbook?pinfl={pinfl}</p>
     *
     * <p>Employment workbook = Government employment history (Mehnat daftarchasi)</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return Employment workbook data
     */
    public Map<String, Object> employmentWorkbook(String pinfl) {
        log.info("Getting employment workbook - PINFL: {}", pinfl);

        Map<String, Object> validationError = validateRequired("pinfl", pinfl);
        if (validationError != null) {
            return validationError;
        }

        // TODO: Call government employment API when integrated
        // EmploymentRecord workbook = employmentApiClient.getWorkbook(pinfl);

        // Mock data
        Map<String, Object> workbook = new HashMap<>();
        workbook.put("pinfl", pinfl);
        workbook.put("full_name", "Alimov Vali Akbarovich");

        List<Map<String, Object>> employmentHistory = new ArrayList<>();

        Map<String, Object> job1 = new HashMap<>();
        job1.put("organization", "Toshkent Davlat Universiteti");
        job1.put("position", "Katta o'qituvchi");
        job1.put("start_date", "2020-09-01");
        job1.put("end_date", null); // Current job
        job1.put("is_current", true);
        employmentHistory.add(job1);

        Map<String, Object> job2 = new HashMap<>();
        job2.put("organization", "O'zbekiston Milliy Universiteti");
        job2.put("position", "Assistent");
        job2.put("start_date", "2018-09-01");
        job2.put("end_date", "2020-08-31");
        job2.put("is_current", false);
        employmentHistory.add(job2);

        workbook.put("employment_history", employmentHistory);
        workbook.put("total_experience_years", 6);

        return successResponse(workbook);
    }

    /**
     * Submit graduate employment data
     *
     * <p><strong>Method:</strong> graduate</p>
     * <p><strong>URL:</strong> POST /app/rest/v2/services/hemishe_EmploymentService/graduate</p>
     *
     * <p><strong>Request Body:</strong> Employment entity (REmployment)</p>
     * <pre>
     * {
     *   "student_pinfl": "12345678901234",
     *   "organization": "Toshkent Davlat Universiteti",
     *   "position": "O'qituvchi",
     *   "employment_date": "2024-09-01",
     *   "employment_type": "Permanent"
     * }
     * </pre>
     *
     * @param employmentData Employment data map
     * @return Success or error
     */
    public Map<String, Object> employmentGraduate(Map<String, Object> employmentData) {
        log.info("Submitting graduate employment - Data: {}", employmentData);

        if (employmentData == null || employmentData.isEmpty()) {
            return errorResponse("invalid_parameter", "Employment data required");
        }

        // Validate required fields
        String pinfl = (String) employmentData.get("student_pinfl");
        if (isEmpty(pinfl)) {
            return errorResponse("invalid_parameter", "student_pinfl required");
        }

        String organization = (String) employmentData.get("organization");
        if (isEmpty(organization)) {
            return errorResponse("invalid_parameter", "organization required");
        }

        // TODO: Save to database when REmployment entity exists
        // REmployment employment = new REmployment();
        // employment.setStudentPinfl(pinfl);
        // employment.setOrganization(organization);
        // ... set other fields
        // employmentRepository.save(employment);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Graduate employment submitted successfully");
        result.put("student_pinfl", pinfl);
        result.put("organization", organization);

        log.info("Graduate employment submitted - PINFL: {}, Organization: {}", pinfl, organization);

        return result;
    }

    /**
     * Submit multiple graduate employments (batch)
     *
     * <p><strong>Method:</strong> graduateList</p>
     * <p><strong>URL:</strong> POST /app/rest/v2/services/hemishe_EmploymentService/graduateList</p>
     *
     * <p><strong>Request Body:</strong> List of Employment entities</p>
     *
     * @param employmentList List of employment data maps
     * @return Success with count or error
     */
    public Map<String, Object> employmentGraduateList(List<Map<String, Object>> employmentList) {
        log.info("Submitting graduate employment list - Count: {}",
                employmentList != null ? employmentList.size() : 0);

        if (employmentList == null || employmentList.isEmpty()) {
            return errorResponse("invalid_parameter", "Employment list required");
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> employmentData : employmentList) {
            try {
                Map<String, Object> result = employmentGraduate(employmentData);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    successCount++;
                } else {
                    errors.add("PINFL " + employmentData.get("student_pinfl") + ": " + result.get("message"));
                }
            } catch (Exception e) {
                log.error("Error processing employment: {}", employmentData, e);
                errors.add("PINFL " + employmentData.get("student_pinfl") + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total_submitted", employmentList.size());
        result.put("success_count", successCount);
        result.put("error_count", errors.size());

        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }

        return result;
    }

    // =====================================================
    // OTM SERVICE (3 methods)
    // =====================================================
    // OTM = Oliy Ta'lim Muassasalari (Higher Education Institutions)
    // Used for data exchange between universities

    /**
     * Get student list by tutor PINFL
     *
     * <p><strong>Method:</strong> studentListByTutor</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_OtmService/studentListByTutor?university={code}&tutorPinfl={pinfl}</p>
     *
     * <p>Returns list of students assigned to specific tutor</p>
     *
     * @param university University code
     * @param tutorPinfl Tutor PINFL
     * @return List of students
     */
    public Map<String, Object> otmStudentListByTutor(String university, String tutorPinfl) {
        log.info("Getting students by tutor - University: {}, Tutor PINFL: {}", university, tutorPinfl);

        Map<String, Object> validationError = validateRequired("university", university);
        if (validationError != null) {
            return validationError;
        }

        validationError = validateRequired("tutorPinfl", tutorPinfl);
        if (validationError != null) {
            return validationError;
        }

        // TODO: Query from database when Tutor-Student relationship exists
        // List<Student> students = studentRepository.findByUniversityAndTutorPinfl(university, tutorPinfl);

        // Mock data
        List<Map<String, Object>> students = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Map<String, Object> student = new HashMap<>();
            student.put("id", UUID.randomUUID());
            student.put("pinfl", "1234567890123" + i);
            student.put("full_name", "Talaba " + i);
            student.put("group", "101-22");
            student.put("course", 2);
            students.add(student);
        }

        return successListResponse(students);
    }

    /**
     * Get student info by ID (for OTM)
     *
     * <p><strong>Method:</strong> studentInfoById</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_OtmService/studentInfoById?studentId={id}</p>
     *
     * @param studentId Student UUID
     * @return Student info
     */
    public Map<String, Object> otmStudentInfoById(String studentId) {
        log.info("Getting student info by ID (OTM) - ID: {}", studentId);

        if (isEmpty(studentId)) {
            return errorResponse("invalid_parameter", "studentId required");
        }

        try {
            UUID uuid = UUID.fromString(studentId);
            var student = studentRepository.findById(uuid);

            if (student.isEmpty()) {
                return notFoundResponse("Student");
            }

            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("id", student.get().getId());
            studentInfo.put("pinfl", student.get().getPinfl());
            studentInfo.put("full_name", student.get().getFirstName() + " " +
                    student.get().getSecondName() + " " + student.get().getThirdName());
            studentInfo.put("university", student.get().getUniversity());
            studentInfo.put("student_status", student.get().getStudentStatus());

            return successResponse(studentInfo);

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID: {}", studentId);
            return errorResponse("invalid_id", "Invalid student ID format");
        }
    }

    /**
     * Get student info by PINFL (for OTM)
     *
     * <p><strong>Method:</strong> studentInfoByPinfl</p>
     * <p><strong>URL:</strong> /app/rest/v2/services/hemishe_OtmService/studentInfoByPinfl?pinfl={pinfl}</p>
     *
     * @param pinfl Student PINFL
     * @return Student info
     */
    public Map<String, Object> otmStudentInfoByPinfl(String pinfl) {
        log.info("Getting student info by PINFL (OTM) - PINFL: {}", pinfl);

        Map<String, Object> validationError = validateRequired("pinfl", pinfl);
        if (validationError != null) {
            return validationError;
        }

        var student = studentRepository.findMasterByPinfl(pinfl);

        if (student.isEmpty()) {
            return notFoundResponse("Student");
        }

        Map<String, Object> studentInfo = new HashMap<>();
        studentInfo.put("id", student.get().getId());
        studentInfo.put("pinfl", student.get().getPinfl());
        studentInfo.put("full_name", student.get().getFirstName() + " " +
                student.get().getSecondName() + " " + student.get().getThirdName());
        studentInfo.put("university", student.get().getUniversity());
        studentInfo.put("student_status", student.get().getStudentStatus());
        studentInfo.put("payment_form", student.get().getPaymentForm());

        return successResponse(studentInfo);
    }
}
