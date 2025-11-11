package uz.hemis.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Student API Controller
 *
 * Permission-based access control examples:
 * - List students: student:read
 * - Create student: student:create
 * - Update student: student:update
 * - Delete student: student:delete
 *
 * OLD-HEMIS PATTERN:
 * - University-based data filtering (row-level security)
 * - Permission-based operation control (column-level security)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
public class StudentController {

    /**
     * List students
     *
     * Permission required: student:read
     * F12 protection: User can only see students from their university (from JWT)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('student:read')")
    public ResponseEntity<?> listStudents(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} listing students", userId);

        // TODO: Filter by university ID from JWT
        // String universityId = jwtTokenProvider.getUniversityIdFromToken(token);
        // List<Student> students = studentService.findByUniversity(universityId);

        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Student list - filtered by university",
            "data", java.util.List.of()
        ));
    }

    /**
     * Get student by ID
     *
     * Permission required: student:read
     * F12 protection: Check if student belongs to user's university
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('student:read')")
    public ResponseEntity<?> getStudent(@PathVariable String id, Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} getting student {}", userId, id);

        // TODO: Verify student belongs to user's university
        // Student student = studentService.findById(id);
        // if (!student.getUniversityId().equals(userUniversityId)) {
        //     throw new AccessDeniedException("Student not in your university");
        // }

        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Student details",
            "data", java.util.Map.of("id", id)
        ));
    }

    /**
     * Create student
     *
     * Permission required: student:create
     * F12 protection: Student will be created in user's university only
     */
    @PostMapping
    @PreAuthorize("hasAuthority('student:create')")
    public ResponseEntity<?> createStudent(@RequestBody java.util.Map<String, Object> request,
                                            Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} creating student", userId);

        // TODO: Force university ID from JWT (prevent tampering)
        // String universityId = jwtTokenProvider.getUniversityIdFromToken(token);
        // request.put("universityId", universityId); // Override client value!

        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Student created in your university"
        ));
    }

    /**
     * Update student
     *
     * Permission required: student:update
     * F12 protection: Check if student belongs to user's university
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('student:update')")
    public ResponseEntity<?> updateStudent(@PathVariable String id,
                                            @RequestBody java.util.Map<String, Object> request,
                                            Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} updating student {}", userId, id);

        // TODO: Verify student belongs to user's university
        // Student student = studentService.findById(id);
        // if (!student.getUniversityId().equals(userUniversityId)) {
        //     throw new AccessDeniedException("Cannot update student from another university");
        // }

        // TODO: Prevent changing university ID (tampering protection)
        // request.remove("universityId"); // Don't allow changing university!

        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Student updated"
        ));
    }

    /**
     * Delete student
     *
     * Permission required: student:delete
     * F12 protection: Check if student belongs to user's university
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('student:delete')")
    public ResponseEntity<?> deleteStudent(@PathVariable String id, Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} deleting student {}", userId, id);

        // TODO: Verify student belongs to user's university
        // Student student = studentService.findById(id);
        // if (!student.getUniversityId().equals(userUniversityId)) {
        //     throw new AccessDeniedException("Cannot delete student from another university");
        // }

        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Student deleted"
        ));
    }

    /**
     * Export students (special permission)
     *
     * Permission required: student:export
     * F12 protection: Export only from user's university
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('student:export')")
    public ResponseEntity<?> exportStudents(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} exporting students", userId);

        // TODO: Filter by university ID from JWT
        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Students exported - filtered by university"
        ));
    }

    /**
     * Combined permission example: Read OR Export
     *
     * User can access if they have EITHER permission
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('student:read', 'student:export')")
    public ResponseEntity<?> getStudentSummary(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} getting student summary", userId);

        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Student summary"
        ));
    }

    /**
     * Multiple permissions required: Read AND Update
     *
     * User must have BOTH permissions
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('student:read') and hasAuthority('student:update')")
    public ResponseEntity<?> updateStudentStatus(@PathVariable String id,
                                                   @RequestBody java.util.Map<String, Object> request,
                                                   Authentication auth) {
        String userId = (String) auth.getPrincipal();
        log.info("User {} updating student {} status", userId, id);

        return ResponseEntity.ok().body(java.util.Map.of(
            "success", true,
            "message", "Student status updated"
        ));
    }
}
