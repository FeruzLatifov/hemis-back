package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Diploma Entity
 *
 * Table: hemishe_e_diploma
 * Purpose: Student diploma/degree certificates
 *
 * Relationships:
 * - Issued to Student
 * - References DiplomaBlank (blank form)
 * - Belongs to University
 *
 * Legacy: Maps to old-HEMIS Diploma entity
 * External Access: /app/rest/diploma/info, /app/rest/diploma/byhash
 */
@Entity
@Table(name = "hemishe_e_diploma")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diploma extends BaseEntity {

    /**
     * Diploma registration number
     * Format: UZ-<year>-<university>-<number>
     */
    @Column(name = "diploma_number", length = 128, unique = true)
    private String diplomaNumber;

    /**
     * Legacy field: _student (Student UUID)
     */
    @Column(name = "_student")
    private UUID student;

    /**
     * Legacy field: _university (University code)
     */
    @Column(name = "_university", length = 64)
    private String university;

    /**
     * Legacy field: _specialty (Specialty UUID)
     */
    @Column(name = "_specialty")
    private UUID specialty;

    /**
     * Legacy field: _diploma_blank (DiplomaBlank UUID)
     */
    @Column(name = "_diploma_blank")
    private UUID diplomaBlank;

    /**
     * Diploma series and number (from blank)
     * Example: "AB 1234567"
     */
    @Column(name = "serial_number", length = 64)
    private String serialNumber;

    /**
     * Diploma type
     * Values: BACHELOR, MASTER, DOCTORATE, DIPLOMA, CERTIFICATE
     */
    @Column(name = "_diploma_type", length = 32)
    private String diplomaType;

    /**
     * Issue date
     */
    @Column(name = "issue_date")
    private LocalDate issueDate;

    /**
     * Registration date in state register
     */
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    /**
     * Graduation year
     */
    @Column(name = "graduation_year")
    private Integer graduationYear;

    /**
     * Academic degree/qualification
     * Example: "Bachelor of Computer Science"
     */
    @Column(name = "qualification", length = 512)
    private String qualification;

    /**
     * GPA / Average grade
     */
    @Column(name = "average_grade")
    private Double averageGrade;

    /**
     * Honors/Distinction
     * Values: NONE, HONORS, HIGH_HONORS, DISTINCTION
     */
    @Column(name = "_honors", length = 32)
    private String honors;

    /**
     * Diploma hash for verification
     * SHA-256 hash of diploma data
     */
    @Column(name = "diploma_hash", length = 128, unique = true)
    private String diplomaHash;

    /**
     * Rector name who signed diploma
     */
    @Column(name = "rector_name", length = 256)
    private String rectorName;

    /**
     * Diploma status
     * Values: DRAFT, ISSUED, REGISTERED, ANNULLED
     */
    @Column(name = "_status", length = 32)
    private String status;

    /**
     * QR code for mobile verification
     */
    @Column(name = "qr_code", length = 512)
    private String qrCode;

    /**
     * Public verification URL
     */
    @Column(name = "verification_url", length = 512)
    private String verificationUrl;

    @Column(name = "notes", length = 2048)
    private String notes;
}
