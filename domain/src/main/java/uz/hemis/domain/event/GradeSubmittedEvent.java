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
 * Grade Submitted Event
 *
 * <p>Published when a teacher submits grades for students</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Notify students about new grades</li>
 *   <li>Update student transcript</li>
 *   <li>Calculate GPA automatically</li>
 *   <li>Trigger academic warnings (low grades)</li>
 *   <li>Update academic progress reports</li>
 *   <li>Audit grade submissions</li>
 * </ul>
 *
 * <p><strong>Event Flow:</strong></p>
 * <pre>
 * GradeService.submitGrade()
 *   → Validate grade value
 *   → Save grade entity
 *   → Update student progress
 *   → Publish GradeSubmittedEvent
 *   → Event listeners react:
 *      - TranscriptService.updateTranscript()
 *      - GPACalculator.recalculateGPA()
 *      - NotificationService.notifyStudent()
 *      - WarningService.checkAcademicStatus()
 * </pre>
 *
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeSubmittedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Grade record ID
     */
    private UUID gradeId;

    /**
     * Student ID
     */
    private UUID studentId;

    /**
     * Student full name
     */
    private String studentFullName;

    /**
     * Student code
     */
    private String studentCode;

    /**
     * Course ID
     */
    private UUID courseId;

    /**
     * Course name
     */
    private String courseName;

    /**
     * Course code
     */
    private String courseCode;

    /**
     * Teacher ID
     */
    private UUID teacherId;

    /**
     * Teacher full name
     */
    private String teacherFullName;

    /**
     * Grade value (e.g., 5, 4, 3, 2, or 100, 95, etc.)
     */
    private Integer gradeValue;

    /**
     * Grade type (exam, midterm, final, coursework, etc.)
     */
    private String gradeType;

    /**
     * Credits for this course
     */
    private Integer credits;

    /**
     * Academic year
     */
    private String academicYear;

    /**
     * Semester (1, 2, or "Fall", "Spring")
     */
    private String semester;

    /**
     * Submission date
     */
    private LocalDate submissionDate;

    /**
     * Is passing grade
     */
    private Boolean isPassing;

    /**
     * Attempt number (1st attempt, 2nd attempt, etc.)
     */
    private Integer attemptNumber;

    /**
     * Grade status (submitted, approved, rejected)
     */
    private String status;

    /**
     * Timestamp when event was created
     */
    private Instant timestamp;

    /**
     * User who submitted the grade
     */
    private String submittedBy;

    /**
     * Additional notes/comments
     */
    private String notes;

    /**
     * Additional metadata (JSON)
     */
    private String metadata;
}
