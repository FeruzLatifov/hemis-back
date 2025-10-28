package uz.hemis.university.final.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.FinalServicesCubaService;

import java.util.List;
import java.util.Map;

/**
 * Final Services API Controller
 *
 * <p><strong>Feature:</strong> Final/Miscellaneous Services</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong></p>
 * <ul>
 *   <li>ScholarshipServiceBean.java - Scholarship management</li>
 *   <li>AttendanceServiceBean.java - Student attendance tracking</li>
 *   <li>UniversityServiceBean.java - University configuration</li>
 *   <li>DoctoralStudentServiceBean.java - Doctoral student management</li>
 * </ul>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Scholarship amount management</li>
 *   <li>Attendance system testing</li>
 *   <li>University system configuration</li>
 *   <li>Doctoral student identification</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * POST /app/rest/v2/services/hemishe_ScholarshipService/deleteAmounts
 * GET /app/rest/v2/services/hemishe_AttendanceService/test
 * GET /app/rest/v2/services/hemishe_UniversityService/config
 * GET /app/rest/v2/services/hemishe_DoctoralStudentService/id
 * </pre>
 *
 * <p><strong>Endpoints:</strong> 4 final service endpoints</p>
 * <p><strong>Users:</strong> Universities, System administrators</p>
 *
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class FinalServicesApiController {

    private final FinalServicesCubaService finalServicesCubaService;

    // =====================================================
    // SCHOLARSHIP SERVICE (1 endpoint)
    // =====================================================

    /**
     * Delete scholarship amounts (batch operation)
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * POST /app/rest/v2/services/hemishe_ScholarshipService/deleteAmounts
     * </pre>
     *
     * <p><strong>Use Case:</strong> University cancels pending scholarship payments</p>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>
     * {
     *   "scholarship_ids": [
     *     "uuid-1234",
     *     "uuid-5678",
     *     "uuid-9012"
     *   ]
     * }
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "deleted_count": 3,
     *   "failed_count": 0,
     *   "message": "Scholarship amounts deleted successfully",
     *   "details": [
     *     {
     *       "id": "uuid-1234",
     *       "status": "deleted",
     *       "amount": 500000
     *     },
     *     {
     *       "id": "uuid-5678",
     *       "status": "deleted",
     *       "amount": 500000
     *     },
     *     {
     *       "id": "uuid-9012",
     *       "status": "deleted",
     *       "amount": 500000
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param request Request containing scholarship IDs to delete
     * @return deletion result with success/failure counts
     */
    @PostMapping("/app/rest/v2/services/hemishe_ScholarshipService/deleteAmounts")
    public ResponseEntity<Map<String, Object>> scholarshipDeleteAmounts(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> scholarshipIds = (List<String>) request.get("scholarship_ids");

        log.info("💰 Scholarship deleteAmounts - Count: {}", scholarshipIds != null ? scholarshipIds.size() : 0);

        String[] idsArray = scholarshipIds != null ? scholarshipIds.toArray(new String[0]) : new String[0];
        Map<String, Object> result = finalServicesCubaService.scholarshipDeleteAmounts(idsArray);

        log.info("✅ Scholarship deleteAmounts completed - Deleted: {}, Failed: {}",
                result.get("deleted_count"), result.get("failed_count"));

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // ATTENDANCE SERVICE (1 endpoint)
    // =====================================================

    /**
     * Test attendance service
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_AttendanceService/test
     * </pre>
     *
     * <p><strong>Use Case:</strong> Verify attendance tracking system is operational</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "service": "AttendanceService",
     *   "status": "operational",
     *   "version": "1.0.0",
     *   "features": [
     *     "mark_attendance",
     *     "get_attendance_report",
     *     "calculate_attendance_rate"
     *   ],
     *   "timestamp": "2024-10-27T10:30:00Z"
     * }
     * </pre>
     *
     * @return attendance service test result
     */
    @GetMapping("/app/rest/v2/services/hemishe_AttendanceService/test")
    public ResponseEntity<Map<String, Object>> attendanceTest() {
        log.debug("🧪 Attendance test");
        Map<String, Object> result = finalServicesCubaService.attendanceTest();
        log.debug("✅ Attendance test completed - Status: {}", result.get("status"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // UNIVERSITY SERVICE (1 endpoint)
    // =====================================================

    /**
     * Get university system configuration
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_UniversityService/config?university={code}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Load university-specific settings and configurations</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "university": {
     *     "code": "00001",
     *     "name": "Toshkent axborot texnologiyalari universiteti",
     *     "short_name": "TATU",
     *     "config": {
     *       "academic_year_start": "09-01",
     *       "semester_count": 2,
     *       "grading_system": "5-point",
     *       "attendance_required": true,
     *       "scholarship_enabled": true,
     *       "contract_payment_enabled": true,
     *       "features": {
     *         "online_registration": true,
     *         "mobile_app": true,
     *         "electronic_diploma": true
     *       },
     *       "limits": {
     *         "max_students_per_group": 30,
     *         "max_courses_per_semester": 8
     *       }
     *     }
     *   }
     * }
     * </pre>
     *
     * @param university University code (5 digits)
     * @return university configuration and settings
     */
    @GetMapping("/app/rest/v2/services/hemishe_UniversityService/config")
    public ResponseEntity<Map<String, Object>> universityConfig(@RequestParam("university") String university) {
        log.info("🏫 University config - University: {}", university);
        Map<String, Object> result = finalServicesCubaService.universityConfig(university);
        log.info("✅ University config completed - Name: {}", result.get("name"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // DOCTORAL STUDENT SERVICE (1 endpoint)
    // =====================================================

    /**
     * Get doctoral student ID by PINFL
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_DoctoralStudentService/id?pinfl={pinfl}
     * </pre>
     *
     * <p><strong>Use Case:</strong> Identify doctoral/PhD student in the system</p>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "exists": true,
     *   "doctoral_student": {
     *     "id": "uuid-1234",
     *     "pinfl": "12345678901234",
     *     "full_name": "Dr. Alisher Karimov",
     *     "university": "TATU",
     *     "speciality": "05.13.01 - Sistemni tahlil qilish",
     *     "supervisor": {
     *       "name": "Prof. Anvar Narzullayev",
     *       "degree": "Doctor of Sciences"
     *     },
     *     "enrollment_year": 2021,
     *     "defense_year": 2024,
     *     "status": "active"
     *   }
     * }
     * </pre>
     *
     * @param pinfl Doctoral student PINFL (14 digits)
     * @return doctoral student identification and details
     */
    @GetMapping("/app/rest/v2/services/hemishe_DoctoralStudentService/id")
    public ResponseEntity<Map<String, Object>> doctoralStudentId(@RequestParam("pinfl") String pinfl) {
        log.info("🎓 DoctoralStudent id - PINFL: {}", pinfl);
        Map<String, Object> result = finalServicesCubaService.doctoralStudentId(pinfl);
        log.info("✅ DoctoralStudent id completed - Exists: {}", result.get("exists"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // ✅ 4 FINAL SERVICE ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Final miscellaneous services
    // Equivalent to: ScholarshipServiceBean, AttendanceServiceBean,
    //                UniversityServiceBean, DoctoralStudentServiceBean
    // =====================================================
}
