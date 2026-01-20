package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Doctoral Student Entity
 *
 * Table: hemishe_e_doctorate_student
 * Purpose: PhD/Doctoral student management
 *
 * CRITICAL - Legacy Table Mapping:
 * - Table: hemishe_e_doctorate_student (ministry.sql)
 * - All column names preserved from OLD-HEMIS
 * - Soft delete: @SQLRestriction("delete_ts IS NULL")
 *
 * NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_doctorate_student")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctoralStudent extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Legacy ID field
    // =====================================================

    /**
     * Legacy U_ID (integer identifier from old system)
     */
    @Column(name = "u_id")
    private Integer uId;

    // =====================================================
    // Personal Information
    // =====================================================

    /**
     * First name
     * Column: first_name VARCHAR(255)
     */
    @Column(name = "first_name", length = 255)
    private String firstName;

    /**
     * Second name (lastname)
     * Column: second_name VARCHAR(255)
     */
    @Column(name = "second_name", length = 255)
    private String secondName;

    /**
     * Third name (patronymic)
     * Column: third_name VARCHAR(255)
     */
    @Column(name = "third_name", length = 255)
    private String thirdName;

    /**
     * Passport number
     * Column: passport_number VARCHAR(255)
     */
    @Column(name = "passport_number", length = 255)
    private String passportNumber;

    /**
     * Passport PIN (PINFL)
     * Column: passport_pin VARCHAR(255)
     */
    @Column(name = "passport_pin", length = 255)
    private String passportPin;

    /**
     * Birth date
     * Column: birth_date DATE
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    // =====================================================
    // Academic Information
    // =====================================================

    /**
     * Dissertation theme
     * Column: dissertation_theme TEXT
     */
    @Column(name = "dissertation_theme", columnDefinition = "TEXT")
    private String dissertationTheme;

    /**
     * Home address
     * Column: home_address VARCHAR(255)
     */
    @Column(name = "home_address", length = 255)
    private String homeAddress;

    /**
     * Accepted date (admission date)
     * Column: accepted_date DATE
     */
    @Column(name = "accepted_date")
    private LocalDate acceptedDate;

    /**
     * Student ID number
     * Column: student_id_number VARCHAR(255)
     */
    @Column(name = "student_id_number", length = 255)
    private String studentIdNumber;

    // =====================================================
    // Classifier References (underscore prefix)
    // =====================================================

    /**
     * Science branch code
     * Column: _science_branch VARCHAR(32)
     */
    @Column(name = "_science_branch", length = 32)
    private String scienceBranch;

    /**
     * Payment form code
     * Column: _payment_form VARCHAR(32)
     */
    @Column(name = "_payment_form", length = 32)
    private String paymentForm;

    /**
     * Citizenship code
     * Column: _citizenship VARCHAR(32)
     */
    @Column(name = "_citizenship", length = 32)
    private String citizenship;

    /**
     * Nationality code
     * Column: _nationality VARCHAR(32)
     */
    @Column(name = "_nationality", length = 32)
    private String nationality;

    /**
     * Gender code
     * Column: _gender VARCHAR(32)
     */
    @Column(name = "_gender", length = 32)
    private String gender;

    /**
     * Country code
     * Column: _country VARCHAR(32)
     */
    @Column(name = "_country", length = 32)
    private String country;

    /**
     * Province
     * Column: _province VARCHAR(255)
     */
    @Column(name = "_province", length = 255)
    private String province;

    /**
     * District
     * Column: _district VARCHAR(255)
     */
    @Column(name = "_district", length = 255)
    private String district;

    /**
     * SOATO code
     * Column: _soato VARCHAR(20)
     */
    @Column(name = "_soato", length = 20)
    private String soato;

    /**
     * Doctoral student type code
     * Column: _doctoral_student_type VARCHAR(32)
     */
    @Column(name = "_doctoral_student_type", length = 32)
    private String doctoralStudentType;

    /**
     * Doctorate student status code
     * Column: _doctorate_student_status VARCHAR(32)
     */
    @Column(name = "_doctorate_student_status", length = 32)
    private String doctorateStudentStatus;

    /**
     * Level
     * Column: _level VARCHAR(255)
     */
    @Column(name = "_level", length = 255)
    private String level;

    /**
     * University code
     * Column: _university VARCHAR(255)
     */
    @Column(name = "_university", length = 255)
    private String university;

    /**
     * Department code
     * Column: _department VARCHAR(255)
     */
    @Column(name = "_department", length = 255)
    private String department;

    /**
     * Position
     * Column: _position INTEGER
     */
    @Column(name = "_position")
    private Integer position;

    /**
     * Active flag
     * Column: active BOOLEAN
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * Translations (JSON)
     * Column: _translations TEXT
     */
    @Column(name = "_translations", columnDefinition = "TEXT")
    private String translations;

    /**
     * Speciality UUID
     * Column: _speciality UUID
     */
    @Column(name = "_speciality")
    private UUID speciality;

    /**
     * Education year code
     * Column: _education_year VARCHAR(32)
     */
    @Column(name = "_education_year", length = 32)
    private String educationYear;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Get full name
     * @return formatted full name
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (secondName != null) sb.append(secondName);
        if (firstName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(firstName);
        }
        if (thirdName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(thirdName);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Check if doctorate student is active
     * @return true if active and not deleted
     */
    public boolean isActiveStudent() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }
}
