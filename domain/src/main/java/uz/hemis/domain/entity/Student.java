package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Student Entity - Mapped to hemishe_e_student table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_student (EXACT name from ministry.sql)</li>
 *   <li>Columns: All names preserved with underscores (_university, _faculty, etc.)</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 *   <li>51 columns total (7 audit + 44 business)</li>
 * </ul>
 *
 * <p><strong>NO RENAME • NO DELETE • NO BREAKING CHANGES</strong></p>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_student")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Student extends BaseEntity {

    private static final long serialVersionUID = 1L;
    
    public String getFirstName() { return firstname; }
    public String getSecondName() { return lastname; }
    public String getThirdName() { return fathername; }
    // NOTE: Latin name columns do not exist in current database schema
    // These methods return null until columns are added via Liquibase migration
    public String getFirstNameLatin() { return null; }
    public String getSecondNameLatin() { return null; }
    public String getThirdNameLatin() { return null; }
    public LocalDate getBirthDate() { return birth_date; }

    // =====================================================
    // Business Key
    // =====================================================

    /**
     * Student code (unique business identifier)
     * Column: code VARCHAR(255) NOT NULL
     */
    @Column(name = "code", nullable = false, length = 255)
    private String code;

    // =====================================================
    // Personal Information
    // =====================================================

    /**
     * First name
     * Column: firstname VARCHAR(255)
     */
    @Column(name = "firstname", length = 255)
    private String firstname;

    /**
     * Last name
     * Column: lastname VARCHAR(255)
     */
    @Column(name = "lastname", length = 255)
    private String lastname;

    /**
     * Father's name (patronymic)
     * Column: fathername VARCHAR(255)
     */
    @Column(name = "fathername", length = 255)
    private String fathername;

    /**
     * PINFL - Personal Identification Number (14 digits)
     * Column: pinfl VARCHAR(255)
     *
     * CRITICAL: PINFL is NOT UNIQUE in old-HEMIS!
     * Multiple students can have same PINFL (managed via isDuplicate flag)
     */
    @Column(name = "pinfl", length = 255)
    private String pinfl;

    /**
     * Duplicate detection flag (Legacy from old-HEMIS)
     * Column: is_duplicate BOOLEAN DEFAULT FALSE
     *
     * Purpose: Manage multiple students with same PINFL
     * - TRUE: Master record (active/current student)
     * - FALSE: Duplicate record (historical/transferred student)
     *
     * CRITICAL: Only ONE student per PINFL can have isDuplicate=true
     *
     * Use case: Student transfers between universities
     *   - Old university record: isDuplicate = false
     *   - New university record: isDuplicate = true
     */
    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    /**
     * Date of birth
     * Column: birthday DATE
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * Birth date (alias for birthday)
     * Column: birthday DATE
     */
    @Column(name = "birthday", insertable = false, updatable = false)
    private LocalDate birth_date;

    // NOTE: Latin name columns do not exist in current database schema
    // Commented out to prevent SQL errors
    // If needed, create Liquibase migration to add these columns

    // @Column(name = "firstname_latin", length = 255)
    // private String firstname_latin;

    // @Column(name = "lastname_latin", length = 255)
    // private String lastname_latin;

    // @Column(name = "fathername_latin", length = 255)
    // private String fathername_latin;

    /**
     * Serial number (passport/ID)
     * Column: serial_number VARCHAR(255)
     */
    @Column(name = "serial_number", length = 255)
    private String serialNumber;

    /**
     * Phone number
     * Column: phone VARCHAR(255)
     */
    @Column(name = "phone", length = 255)
    private String phone;

    // =====================================================
    // Location Information
    // =====================================================

    /**
     * Permanent address
     * Column: address VARCHAR(1024)
     */
    @Column(name = "address", length = 1024)
    private String address;

    /**
     * Current address
     * Column: current_address VARCHAR(1024)
     */
    @Column(name = "current_address", length = 1024)
    private String currentAddress;

    /**
     * SOATO code (location classifier)
     * Column: _soato VARCHAR(20)
     */
    @Column(name = "_soato", length = 20)
    private String soato;

    /**
     * Current SOATO code
     * Column: _current_soato VARCHAR(20)
     */
    @Column(name = "_current_soato", length = 20)
    private String currentSoato;

    // =====================================================
    // University References (VARCHAR - soft references)
    // =====================================================
    // CRITICAL: These are VARCHAR codes, NOT UUIDs
    // They reference classifier tables (hemishe_h_*)
    // NO foreign keys in CUBA design pattern
    // =====================================================

    /**
     * University code
     * Column: _university VARCHAR(255)
     * References: hemishe_e_university.code (VARCHAR PK)
     */
    @Column(name = "_university", length = 255)
    private String university;

    /**
     * Faculty code
     * Column: _faculty VARCHAR(255)
     * References: hemishe_e_university_department.code
     */
    @Column(name = "_faculty", length = 255)
    private String faculty;

    /**
     * Speciality code
     * Column: _speciality VARCHAR(255)
     * References: hemishe_e_speciality.code
     */
    @Column(name = "_speciality", length = 255)
    private String speciality;

    // =====================================================
    // Education Classifiers (VARCHAR codes)
    // =====================================================

    /**
     * Student status code
     * Column: _student_status VARCHAR(32)
     * References: hemishe_h_student_status.code
     * Examples: '11' = active, '16' = graduated, etc.
     */
    @Column(name = "_student_status", length = 32)
    private String studentStatus;

    /**
     * Payment form code
     * Column: _payment_form VARCHAR(32)
     * References: hemishe_h_payment_form.code
     * Examples: '11' = budget, '12' = contract, etc.
     */
    @Column(name = "_payment_form", length = 32)
    private String paymentForm;

    /**
     * Education type code
     * Column: _education_type VARCHAR(32)
     * References: hemishe_h_education_type.code
     * Examples: '11' = bachelor, '12' = master, '13' = doctoral, etc.
     */
    @Column(name = "_education_type", length = 32)
    private String educationType;

    /**
     * Education form code
     * Column: _education_form VARCHAR(32)
     * References: hemishe_h_education_form.code
     * Examples: '11' = full-time, '12' = part-time, '13' = evening, etc.
     */
    @Column(name = "_education_form", length = 32)
    private String educationForm;

    /**
     * Course code (year level)
     * Column: _course VARCHAR(32)
     * References: hemishe_h_course.code
     * Examples: '1', '2', '3', '4', etc.
     */
    @Column(name = "_course", length = 32)
    private String course;

    /**
     * Education year code
     * Column: _education_year VARCHAR(32)
     * References: hemishe_h_education_year.code
     * Examples: '2023', '2024', etc.
     */
    @Column(name = "_education_year", length = 32)
    private String educationYear;

    /**
     * Current education year code
     * Column: _current_education_year_code VARCHAR(32)
     */
    @Column(name = "_current_education_year_code", length = 32)
    private String currentEducationYearCode;

    /**
     * Gender code
     * Column: _gender VARCHAR(32)
     * References: hemishe_h_gender.code
     * Examples: '1' = male, '2' = female
     */
    @Column(name = "_gender", length = 32)
    private String gender;

    /**
     * Nationality code
     * Column: _nationality VARCHAR(32)
     * References: hemishe_h_nationality.code
     */
    @Column(name = "_nationality", length = 32)
    private String nationality;

    /**
     * Citizenship code
     * Column: _citizenship VARCHAR(32)
     * References: hemishe_h_country.code
     */
    @Column(name = "_citizenship", length = 32)
    private String citizenship;

    /**
     * Country code
     * Column: _country VARCHAR(32)
     * References: hemishe_h_country.code
     */
    @Column(name = "_country", length = 32)
    private String country;

    /**
     * Language code
     * Column: _language VARCHAR(32)
     * References: hemishe_h_language.code
     */
    @Column(name = "_language", length = 32)
    private String language;

    /**
     * Accommodation type code
     * Column: _accomodation VARCHAR(32)
     * Note: Typo "accomodation" is in original schema (1 'm')
     */
    @Column(name = "_accomodation", length = 32)
    private String accomodation;

    /**
     * Living status code
     * Column: _living_status VARCHAR(32)
     */
    @Column(name = "_living_status", length = 32)
    private String livingStatus;

    /**
     * Roommate type code
     * Column: _roommate_type VARCHAR(32)
     */
    @Column(name = "_roommate_type", length = 32)
    private String roommateType;

    /**
     * Social category code
     * Column: _social_category VARCHAR(32)
     */
    @Column(name = "_social_category", length = 32)
    private String socialCategory;

    /**
     * Stipend rate code
     * Column: _stipend_rate VARCHAR(32)
     */
    @Column(name = "_stipend_rate", length = 32)
    private String stipendRate;

    /**
     * Expel reason code (if expelled)
     * Column: _expel_reason VARCHAR(32)
     */
    @Column(name = "_expel_reason", length = 32)
    private String expelReason;

    /**
     * Doctoral student type code
     * Column: _doctoral_student_type VARCHAR(32)
     */
    @Column(name = "_doctoral_student_type", length = 32)
    private String doctoralStudentType;

    // =====================================================
    // Speciality UUID References (for multi-level education)
    // =====================================================

    /**
     * Bachelor speciality UUID
     * Column: _speciality_bachelor UUID
     * References: hemishe_e_speciality.id
     */
    @Column(name = "_speciality_bachelor")
    private UUID specialityBachelor;

    /**
     * Master speciality UUID
     * Column: _speciality_master UUID
     * References: hemishe_e_speciality.id
     */
    @Column(name = "_speciality_master")
    private UUID specialityMaster;

    /**
     * Doctoral speciality UUID
     * Column: _speciality_doctoral UUID
     * References: hemishe_e_speciality.id
     */
    @Column(name = "_speciality_doctoral")
    private UUID specialityDoctoral;

    // =====================================================
    // Additional Fields
    // =====================================================

    /**
     * Generic status field
     * Column: status VARCHAR(255)
     */
    @Column(name = "status", length = 255)
    private String status;

    /**
     * Tag field (for filtering/grouping)
     * Column: tag VARCHAR(255)
     */
    @Column(name = "tag", length = 255)
    private String tag;

    /**
     * Active flag
     * Column: active BOOLEAN
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * Roommate count
     * Column: roommate_count INTEGER
     */
    @Column(name = "roommate_count")
    private Integer roommateCount;

    /**
     * Responsible person phone
     * Column: responsible_person_phone VARCHAR(255)
     */
    @Column(name = "responsible_person_phone", length = 255)
    private String responsiblePersonPhone;

    // =====================================================
    // Additional Boolean Flags (from ministry.sql)
    // =====================================================

    /**
     * Verified flag (for verification system)
     * Column: verified BOOLEAN
     */
    @Column(name = "verified")
    private Boolean verified;

    /**
     * Points (for verification system)
     * Column: points VARCHAR(255)
     */
    @Column(name = "points", length = 255)
    private String points;

    // =====================================================
    // Contact Fields (from old-hemis)
    // =====================================================

    /**
     * Email address
     * Column: email VARCHAR(255)
     */
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Parent phone
     * Column: parent_phone VARCHAR(255)
     */
    @Column(name = "parent_phone", length = 255)
    private String parentPhone;

    /**
     * Geo address
     * Column: geo_address VARCHAR(1024)
     */
    @Column(name = "geo_address", length = 1024)
    private String geoAddress;

    // =====================================================
    // Group Fields
    // =====================================================

    /**
     * Group ID
     * Column: group_id VARCHAR(255)
     */
    @Column(name = "group_id", length = 255)
    private String groupId;

    /**
     * Group name
     * Column: group_name VARCHAR(255)
     */
    @Column(name = "group_name", length = 255)
    private String groupName;

    // =====================================================
    // Graduate and Status Fields
    // =====================================================

    /**
     * Is graduate flag
     * Column: is_graduate VARCHAR(10)
     */
    @Column(name = "is_graduate", length = 10)
    private String isGraduate;

    /**
     * Passport given date
     * Column: passport_given_date DATE
     */
    @Column(name = "passport_given_date")
    private LocalDate passportGivenDate;

    // =====================================================
    // Enrollment Order Fields
    // =====================================================

    /**
     * Enroll order name
     * Column: enroll_order_name VARCHAR(1024)
     */
    @Column(name = "enroll_order_name", length = 1024)
    private String enrollOrderName;

    /**
     * Enroll order date
     * Column: enroll_order_date DATE
     */
    @Column(name = "enroll_order_date")
    private LocalDate enrollOrderDate;

    /**
     * Enroll order number
     * Column: enroll_order_number VARCHAR(255)
     */
    @Column(name = "enroll_order_number", length = 255)
    private String enrollOrderNumber;

    /**
     * Enroll order category
     * Column: enroll_order_category VARCHAR(255)
     */
    @Column(name = "enroll_order_category", length = 255)
    private String enrollOrderCategory;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Get full name (lastname firstname fathername)
     *
     * @return full name in traditional order
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastname != null) sb.append(lastname);
        if (firstname != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(firstname);
        }
        if (fathername != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(fathername);
        }
        return sb.toString();
    }

    /**
     * Check if student is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActiveStudent() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    // =====================================================
    // NOTE: NO DELETE METHODS
    // =====================================================
    // NDG (Non-Deletion Guarantee) - no physical DELETE
    // Soft delete handled at service layer by setting deleteTs
    // =====================================================
}
