package uz.hemis.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Student Enrolled Event
 *
 * <p>Published when a new student is successfully enrolled in the system</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Send welcome email/SMS to student</li>
 *   <li>Create student account in external systems</li>
 *   <li>Trigger onboarding workflow</li>
 *   <li>Update statistics and reports</li>
 *   <li>Audit trail and logging</li>
 * </ul>
 *
 * <p><strong>Event Flow:</strong></p>
 * <pre>
 * StudentService.enrollStudent()
 *   → Save student entity
 *   → Publish StudentEnrolledEvent
 *   → Event listeners react:
 *      - EmailService.sendWelcomeEmail()
 *      - NotificationService.notifyAdmins()
 *      - AuditService.logEnrollment()
 * </pre>
 *
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrolledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Student ID
     */
    private UUID studentId;

    /**
     * Student code (e.g., "2024010001")
     */
    private String studentCode;

    /**
     * Full name
     */
    private String fullName;

    /**
     * Email address
     */
    private String email;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Faculty/Department ID
     */
    private UUID facultyId;

    /**
     * Faculty name
     */
    private String facultyName;

    /**
     * Specialty ID
     */
    private UUID specialtyId;

    /**
     * Specialty name
     */
    private String specialtyName;

    /**
     * Enrollment date
     */
    private LocalDate enrollmentDate;

    /**
     * Academic year (e.g., "2024-2025")
     */
    private String academicYear;

    /**
     * Education form (full-time, part-time, etc.)
     */
    private String educationForm;

    /**
     * Timestamp when event was created
     */
    private Instant timestamp;

    /**
     * User who performed the enrollment
     */
    private String enrolledBy;

    /**
     * Additional metadata
     */
    private String metadata;
}
