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
 * Diploma Issued Event
 *
 * <p>Published when a diploma is successfully issued to a graduate</p>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Send diploma ready notification</li>
 *   <li>Report to Ministry of Education</li>
 *   <li>Update diploma registry</li>
 *   <li>Trigger printing workflow</li>
 *   <li>Record in blockchain (if applicable)</li>
 *   <li>Audit and compliance logging</li>
 * </ul>
 *
 * <p><strong>Event Flow:</strong></p>
 * <pre>
 * DiplomaService.issueDiploma()
 *   → Validate student eligibility
 *   → Assign diploma blank number
 *   → Save diploma entity
 *   → Publish DiplomaIssuedEvent
 *   → Event listeners react:
 *      - MinistryReportingService.reportDiploma()
 *      - PrintingService.queueForPrinting()
 *      - NotificationService.notifyGraduate()
 * </pre>
 *
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiplomaIssuedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Diploma ID
     */
    private UUID diplomaId;

    /**
     * Diploma series and number (e.g., "AA 1234567")
     */
    private String diplomaNumber;

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
     * Specialty name
     */
    private String specialtyName;

    /**
     * Specialty code (classifier)
     */
    private String specialtyCode;

    /**
     * Qualification level (Bachelor, Master, etc.)
     */
    private String qualificationLevel;

    /**
     * Issue date
     */
    private LocalDate issueDate;

    /**
     * Graduation date
     */
    private LocalDate graduationDate;

    /**
     * GPA / Average grade
     */
    private Double averageGrade;

    /**
     * Honors (with distinction, etc.)
     */
    private String honors;

    /**
     * University ID
     */
    private UUID universityId;

    /**
     * University name
     */
    private String universityName;

    /**
     * Diploma blank ID (physical blank used)
     */
    private UUID diplomaBlankId;

    /**
     * Timestamp when event was created
     */
    private Instant timestamp;

    /**
     * User who issued the diploma
     */
    private String issuedBy;

    /**
     * Additional metadata (JSON)
     */
    private String metadata;
}
