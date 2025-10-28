package uz.hemis.university.teacher.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.university.service.TeacherCubaService;

import java.util.Map;

/**
 * Teacher API Controller
 *
 * <p><strong>Feature:</strong> Teacher Management</p>
 * <p><strong>OLD-HEMIS Equivalent:</strong> TeacherServiceBean.java (37 separate ServiceBeans pattern)</p>
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Teacher profile management</li>
 *   <li>Teacher employment and job assignments</li>
 *   <li>Teacher verification and retrieval</li>
 * </ul>
 *
 * <p><strong>CUBA Pattern (Backward Compatible):</strong></p>
 * <pre>
 * GET /app/rest/v2/services/hemishe_TeacherService/{methodName}
 * → TeacherCubaService.{methodName}()
 * </pre>
 *
 * <p><strong>Endpoints:</strong> 4 teacher-specific endpoints</p>
 * <p><strong>Users:</strong> 200+ universities across Uzbekistan</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/hemishe_TeacherService")
@RequiredArgsConstructor
@Slf4j
public class TeacherApiController {

    private final TeacherCubaService teacherCubaService;

    /**
     * Get teacher ID by search criteria
     *
     * <p><strong>OLD-HEMIS URL:</strong></p>
     * <pre>
     * GET /app/rest/v2/services/hemishe_TeacherService/id?data={data}
     * </pre>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * curl 'https://ministry.hemis.uz/app/rest/v2/services/hemishe_TeacherService/id?data=...' \
     *   -H 'Authorization: Bearer {token}'
     * </pre>
     *
     * <p><strong>Response:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "id": "uuid",
     *   "teacher": {...}
     * }
     * </pre>
     *
     * @param data Search criteria (JSON string with name, PINFL, passport, etc.)
     * @return teacher ID and basic information if found
     */
    @GetMapping("/id")
    public ResponseEntity<Map<String, Object>> id(@RequestParam("data") String data) {
        log.info("🔍 Teacher id - Data: {}", data);
        Map<String, Object> result = teacherCubaService.id(data);
        log.info("✅ Teacher id search completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    /**
     * Get teacher by internal ID
     *
     * <p>Returns full teacher information including employment, positions, and academic degree</p>
     *
     * @param id Teacher ID (UUID format)
     * @return complete teacher profile
     */
    @GetMapping("/getById")
    public ResponseEntity<Map<String, Object>> getById(@RequestParam("id") String id) {
        log.info("👤 Teacher getById - ID: {}", id);
        return ResponseEntity.ok(teacherCubaService.getById(id));
    }

    /**
     * Get teacher by PINFL
     *
     * <p>Retrieves teacher information using PINFL (Personal Identification Number)</p>
     *
     * @param pinfl PINFL (14 digits)
     * @return teacher profile with employment details
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> get(@RequestParam("pinfl") String pinfl) {
        log.info("👤 Teacher get - PINFL: {}", pinfl);
        return ResponseEntity.ok(teacherCubaService.get(pinfl));
    }

    /**
     * Add job/position to teacher
     *
     * <p><strong>Creates new employment record for teacher including:</strong></p>
     * <ul>
     *   <li>Position and department</li>
     *   <li>Employment start date</li>
     *   <li>Work hours and rate</li>
     *   <li>Contract type (full-time, part-time, etc.)</li>
     * </ul>
     *
     * <p><strong>Request Body Example:</strong></p>
     * <pre>
     * {
     *   "teacher_id": "uuid",
     *   "position_code": "PROFESSOR",
     *   "cathedra_id": "uuid",
     *   "start_date": "2024-09-01",
     *   "rate": 1.0,
     *   "contract_type": "BASIC"
     * }
     * </pre>
     *
     * @param jobData Job/employment information
     * @return creation result with job ID
     */
    @PostMapping("/addJob")
    public ResponseEntity<Map<String, Object>> addJob(@RequestBody Map<String, Object> jobData) {
        log.info("💼 Teacher addJob - Job data received");
        Map<String, Object> result = teacherCubaService.addJob(jobData);
        log.info("✅ Teacher addJob completed - Success: {}", result.get("success"));
        return ResponseEntity.ok(result);
    }

    // =====================================================
    // ✅ 4 TEACHER ENDPOINTS IMPLEMENTED
    // =====================================================
    // Following OLD-HEMIS pattern: Separate controller per feature
    // Equivalent to: TeacherServiceBean.java in CUBA Platform
    // =====================================================
}
