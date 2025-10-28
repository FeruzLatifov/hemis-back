package uz.hemis.university.integration.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.IntegrationCubaService;

import java.util.List;
import java.util.Map;

/**
 * Integration API Controller
 *
 * <p><strong>Feature:</strong> External System Integrations</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong></p>
 * <ul>
 *   <li>EmploymentServiceBean.java - Graduate employment tracking</li>
 *   <li>OtmServiceBean.java - OTM (Higher Education Institution) integration</li>
 * </ul>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Graduate employment registration</li>
 *   <li>Employment workbook data</li>
 *   <li>OTM student information exchange</li>
 *   <li>Tutor-student relationship tracking</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_EmploymentService/workbook
 * POST /app/rest/v2/services/hemishe_EmploymentService/graduate
 * GET /app/rest/v2/services/hemishe_OtmService/studentListByTutor
 * </pre>
 *
 * <p><strong>Endpoints:</strong> 6 integration endpoints</p>
 * <p><strong>Users:</strong> 200+ universities, Ministry of Employment, OTM system</p>
 *
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class IntegrationApiController {

    private final IntegrationCubaService integrationCubaService;

    // =====================================================
    // EMPLOYMENT SERVICE (3 endpoints)
    // =====================================================

    /**
     * Get employment workbook for graduate
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_EmploymentService/workbook?pinfl={pinfl}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Retrieve employment history for a graduate</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_EmploymentService/workbook?pinfl=12345678901234' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "workbook": {
     *     "pinfl": "12345678901234",
     *     "full_name": "Javohir Ergashev",
     *     "employment_history": [
     *       {
     *         "organization": "IT Company LLC",
     *         "position": "Software Engineer",
     *         "start_date": "2024-07-01",
     *         "end_date": null,
     *         "status": "active"
     *       }
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param pinfl Graduate PINFL (14 digits)
     * @return employment workbook with job history
     */
    @GetMapping("/app/rest/v2/services/hemishe_EmploymentService/workbook")
    public ResponseEntity<Map<String, Object>> employmentWorkbook(@RequestParam("pinfl") String pinfl) {
        log.info("💼 Employment workbook - PINFL: {}", pinfl);
        Map<String, Object> result = integrationCubaService.employmentWorkbook(pinfl);
        log.info("✅ Employment workbook completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    /**
     * Submit graduate employment information
     *
     * <p><strong>Use Case:</strong> University reports graduate's first job placement</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>
     * {
     *   "pinfl": "12345678901234",
     *   "organization_tin": "123456789",
     *   "organization_name": "IT Company LLC",
     *   "position": "Software Engineer",
     *   "start_date": "2024-07-01",
     *   "salary": 5000000,
     *   "contract_type": "full_time"
     * }
     * </pre>
     *
     * @param employmentData Graduate employment information
     * @return submission result with employment ID
     */
    @PostMapping("/app/rest/v2/services/hemishe_EmploymentService/graduate")
    public ResponseEntity<Map<String, Object>> employmentGraduate(@RequestBody Map<String, Object> employmentData) {
        log.info("💼 Employment graduate - Submitting employment data");
        Map<String, Object> result = integrationCubaService.employmentGraduate(employmentData);
        log.info("✅ Employment graduate completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    /**
     * Submit multiple graduate employment records (batch)
     *
     * <p><strong>Use Case:</strong> University bulk reports graduate employment</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>
     * [
     *   {
     *     "pinfl": "12345678901234",
     *     "organization_tin": "123456789",
     *     "position": "Software Engineer",
     *     ...
     *   },
     *   {
     *     "pinfl": "98765432109876",
     *     "organization_tin": "987654321",
     *     "position": "Accountant",
     *     ...
     *   }
     * ]
     * </pre>
     *
     * @param employmentList List of employment records
     * @return batch processing result with success/failure counts
     */
    @PostMapping("/app/rest/v2/services/hemishe_EmploymentService/graduateList")
    public ResponseEntity<Map<String, Object>> employmentGraduateList(
            @RequestBody List<Map<String, Object>> employmentList) {
        log.info("💼 Employment graduateList - Count: {}", employmentList != null ? employmentList.size() : 0);
        Map<String, Object> result = integrationCubaService.employmentGraduateList(employmentList);
        log.info("✅ Employment graduateList completed - Success: {}, Failed: {}",
                result.get("success_count"), result.get("failed_count"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // OTM SERVICE (3 endpoints)
    // =====================================================

    /**
     * Get list of students by tutor (academic advisor)
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_OtmService/studentListByTutor?university={code}&tutorPinfl={pinfl}
     * </pre>
     *
     * <p><strong>Use Case:</strong> OTM system queries students assigned to a specific tutor</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "tutor": {
     *     "pinfl": "11111111111111",
     *     "full_name": "Dr. Alisher Karimov"
     *   },
     *   "students": [
     *     {
     *       "id": "uuid-1",
     *       "pinfl": "22222222222222",
     *       "full_name": "Javohir Ergashev",
     *       "group": "101-guruh",
     *       "course": 2,
     *       "status": "active"
     *     },
     *     ...
     *   ],
     *   "total": 15
     * }
     * </pre>
     *
     * @param university University code
     * @param tutorPinfl Tutor PINFL (academic advisor)
     * @return list of students assigned to the tutor
     */
    @GetMapping("/app/rest/v2/services/hemishe_OtmService/studentListByTutor")
    public ResponseEntity<Map<String, Object>> otmStudentListByTutor(
            @RequestParam("university") String university,
            @RequestParam("tutorPinfl") String tutorPinfl) {
        log.info("🏫 OTM studentListByTutor - University: {}, Tutor: {}", university, tutorPinfl);
        Map<String, Object> result = integrationCubaService.otmStudentListByTutor(university, tutorPinfl);
        log.info("✅ OTM studentListByTutor completed - Count: {}", result.get("total"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get student information by internal ID (OTM format)
     *
     * <p><strong>Use Case:</strong> OTM system queries full student profile</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "student": {
     *     "id": "uuid-1234",
     *     "pinfl": "12345678901234",
     *     "full_name": "Javohir Ergashev",
     *     "education_type": "11",
     *     "faculty": "Informatika",
     *     "speciality": "Dasturiy injiniring",
     *     "group": "101-guruh",
     *     "course": 2,
     *     "tutor": {
     *       "pinfl": "11111111111111",
     *       "full_name": "Dr. Alisher Karimov"
     *     },
     *     "gpa": 4.5,
     *     "status": "active"
     *   }
     * }
     * </pre>
     *
     * @param studentId Student internal ID (UUID)
     * @return complete student profile in OTM format
     */
    @GetMapping("/app/rest/v2/services/hemishe_OtmService/studentInfoById")
    public ResponseEntity<Map<String, Object>> otmStudentInfoById(@RequestParam("studentId") String studentId) {
        log.info("🏫 OTM studentInfoById - ID: {}", studentId);
        return ResponseEntity.ok(integrationCubaService.otmStudentInfoById(studentId));
    }

    /**
     * Get student information by PINFL (OTM format)
     *
     * <p><strong>Use Case:</strong> OTM system queries student by national ID</p>
     *
     * @param pinfl Student PINFL (14 digits)
     * @return student profile in OTM format
     */
    @GetMapping("/app/rest/v2/services/hemishe_OtmService/studentInfoByPinfl")
    public ResponseEntity<Map<String, Object>> otmStudentInfoByPinfl(@RequestParam("pinfl") String pinfl) {
        log.info("🏫 OTM studentInfoByPinfl - PINFL: {}", pinfl);
        return ResponseEntity.ok(integrationCubaService.otmStudentInfoByPinfl(pinfl));
    }

    // =====================================================
    // ✅ 6 INTEGRATION ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Grouped integration services
    // Equivalent to: EmploymentServiceBean, OtmServiceBean
    // =====================================================
}
