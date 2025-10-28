package uz.hemis.university.reference.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.ReferenceDataCubaService;

import java.util.Map;

/**
 * Reference Data API Controller
 *
 * <p><strong>Feature:</strong> University Reference Data (Organizational Structure)</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong></p>
 * <ul>
 *   <li>FacultyServiceBean.java - Faculty management</li>
 *   <li>CathedraServiceBean.java - Department management</li>
 *   <li>SpecialityServiceBean.java - Speciality/Program management</li>
 *   <li>GroupServiceBean.java - Student group management</li>
 * </ul>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Faculty (fakultet) hierarchies</li>
 *   <li>Cathedra (kafedra) - academic departments</li>
 *   <li>Specialities - degree programs and majors</li>
 *   <li>Groups - student groups and cohorts</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_FacultyService/get
 * GET /app/rest/v2/services/hemishe_CathedraService/get
 * GET /app/rest/v2/services/hemishe_SpecialityService/get
 * GET /app/rest/v2/services/hemishe_GroupService/get
 * </pre>
 *
 * <p><strong>Endpoints:</strong> 4 reference data services</p>
 * <p><strong>Users:</strong> 200+ universities across Uzbekistan</p>
 *
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ReferenceDataApiController {

    private final ReferenceDataCubaService referenceDataCubaService;

    /**
     * Get faculties for university
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_FacultyService/get?university={code}
     * </pre>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_FacultyService/get?university=00001' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "faculties": [
     *     {
     *       "id": "uuid",
     *       "code": "FIZ-MAT",
     *       "name": "Fizika-matematika fakulteti",
     *       "name_latin": "Physics and Mathematics Faculty"
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param university University code (5 digits)
     * @return list of faculties for the university
     */
    @GetMapping("/app/rest/v2/services/hemishe_FacultyService/get")
    public ResponseEntity<Map<String, Object>> getFaculties(@RequestParam("university") String university) {
        log.info("🏛️ Faculty get - University: {}", university);
        Map<String, Object> result = referenceDataCubaService.getFaculties(university);
        log.info("✅ Faculty get completed - Count: {}",
            result.get("faculties") != null ? ((java.util.List<?>) result.get("faculties")).size() : 0);
        return ResponseEntity.ok(result);
    }

    /**
     * Get cathedras (departments) for university
     *
     * <p>Returns all academic departments (kafedra) for the specified university</p>
     *
     * <p><strong>Example Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "cathedras": [
     *     {
     *       "id": "uuid",
     *       "code": "OLIY-MAT",
     *       "name": "Oliy matematika kafedrasi",
     *       "faculty_id": "uuid",
     *       "faculty_name": "Fizika-matematika fakulteti"
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param university University code (5 digits)
     * @return list of cathedras with faculty associations
     */
    @GetMapping("/app/rest/v2/services/hemishe_CathedraService/get")
    public ResponseEntity<Map<String, Object>> getCathedras(@RequestParam("university") String university) {
        log.info("🏫 Cathedra get - University: {}", university);
        Map<String, Object> result = referenceDataCubaService.getCathedras(university);
        log.info("✅ Cathedra get completed - Count: {}",
            result.get("cathedras") != null ? ((java.util.List<?>) result.get("cathedras")).size() : 0);
        return ResponseEntity.ok(result);
    }

    /**
     * Get specialities (degree programs) for university
     *
     * <p>Returns degree programs filtered by education type</p>
     *
     * <p><strong>Education Types:</strong></p>
     * <ul>
     *   <li>11 - Bachelor's</li>
     *   <li>12 - Master's</li>
     *   <li>13 - Doctoral/PhD</li>
     * </ul>
     *
     * <p><strong>Example Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "specialities": [
     *     {
     *       "id": "uuid",
     *       "code": "60310100",
     *       "name": "Matematika",
     *       "education_type": "11",
     *       "faculty_id": "uuid"
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param university University code (5 digits)
     * @param type Education type code (11=Bachelor, 12=Master, 13=PhD)
     * @return list of specialities for specified education type
     */
    @GetMapping("/app/rest/v2/services/hemishe_SpecialityService/get")
    public ResponseEntity<Map<String, Object>> getSpecialities(
            @RequestParam("university") String university,
            @RequestParam("type") String type) {
        log.info("📚 Speciality get - University: {}, Type: {}", university, type);
        Map<String, Object> result = referenceDataCubaService.getSpecialities(university, type);
        log.info("✅ Speciality get completed - Count: {}",
            result.get("specialities") != null ? ((java.util.List<?>) result.get("specialities")).size() : 0);
        return ResponseEntity.ok(result);
    }

    /**
     * Get student groups for university
     *
     * <p>Returns student groups filtered by education type and academic year</p>
     *
     * <p><strong>Example Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "groups": [
     *     {
     *       "id": "uuid",
     *       "code": "101-guruh",
     *       "name": "Matematika 1-kurs 101-guruh",
     *       "education_type": "11",
     *       "education_year": "2024",
     *       "course": 1,
     *       "speciality_id": "uuid",
     *       "student_count": 25
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param university University code (5 digits)
     * @param type Education type code (11=Bachelor, 12=Master, 13=PhD)
     * @param year Academic year (e.g., "2024")
     * @return list of student groups for specified criteria
     */
    @GetMapping("/app/rest/v2/services/hemishe_GroupService/get")
    public ResponseEntity<Map<String, Object>> getGroups(
            @RequestParam("university") String university,
            @RequestParam("type") String type,
            @RequestParam("year") String year) {
        log.info("👥 Group get - University: {}, Type: {}, Year: {}", university, type, year);
        Map<String, Object> result = referenceDataCubaService.getGroups(university, type, year);
        log.info("✅ Group get completed - Count: {}",
            result.get("groups") != null ? ((java.util.List<?>) result.get("groups")).size() : 0);
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // ✅ 4 REFERENCE DATA ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Separate services per entity type
    // Equivalent to: FacultyServiceBean, CathedraServiceBean,
    //                SpecialityServiceBean, GroupServiceBean in CUBA
    // Grouped together as they're all university reference data
    // =====================================================
}
