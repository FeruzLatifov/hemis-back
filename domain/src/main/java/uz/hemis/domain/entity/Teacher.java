package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

/**
 * Teacher Entity - Mapped to hemishe_e_teacher table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_teacher</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Extends {@link BaseEntity} for CUBA audit pattern.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_teacher")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Teacher extends BaseEntity {

    private static final long serialVersionUID = 1L;
    
    public String getPinfl() { return pinfl; }
    public void setPinfl(String pinfl) { this.pinfl = pinfl; }
    public String getFirstName() { return firstname; }
    public String getSecondName() { return lastname; }
    public String getThirdName() { return fathername; }
    public LocalDate getBirthDate() { return birthday; }
    public String getCitizenship() { return citizenship; }

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
     * Date of birth
     * Column: birthday DATE
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * PINFL - Personal Identification Number
     * Column: pinfl VARCHAR(255)
     */
    @Column(name = "pinfl", length = 255)
    private String pinfl;

    /**
     * Citizenship code
     * Column: _citizenship VARCHAR(32)
     * References: hemishe_h_citizenship.code
     */
    @Column(name = "_citizenship", length = 32)
    private String citizenship;

    /**
     * Gender code
     * Column: _gender VARCHAR(32)
     * References: hemishe_h_gender.code
     */
    @Column(name = "_gender", length = 32)
    private String gender;

    /**
     * Teacher code (unique identifier)
     * Column: code VARCHAR(255)
     */
    @Column(name = "code", length = 255)
    private String code;

    /**
     * Passport serial number
     * Column: serial_number VARCHAR(255)
     */
    @Column(name = "serial_number", length = 255)
    private String serialNumber;

    /**
     * Address
     * Column: address VARCHAR(1024)
     */
    @Column(name = "address", length = 1024)
    private String address;

    /**
     * Phone number
     * Column: phone VARCHAR(1024)
     */
    @Column(name = "phone", length = 1024)
    private String phone;

    /**
     * Employment year
     * Column: employee_year VARCHAR(255)
     */
    @Column(name = "employee_year", length = 255)
    private String employeeYear;

    /**
     * Tag for versioning
     * Column: tag VARCHAR(255)
     */
    @Column(name = "tag", length = 255)
    private String tag;

    // =====================================================
    // University Reference
    // =====================================================

    /**
     * University code
     * Column: _university VARCHAR(255)
     * References: hemishe_e_university.code (VARCHAR PK)
     */
    @Column(name = "_university", length = 255)
    private String university;

    // =====================================================
    // Academic Qualifications
    // =====================================================

    /**
     * Academic degree code
     * Column: _academic_degree VARCHAR(32)
     * References: hemishe_h_academic_degree.code
     * Examples: '11' = candidate, '12' = doctor, '13' = DSc, etc.
     */
    @Column(name = "_academic_degree", length = 32)
    private String academicDegree;

    /**
     * Academic rank code
     * Column: _academic_rank VARCHAR(32)
     * References: hemishe_h_academic_rank.code
     * Examples: '11' = assistant, '12' = senior lecturer, '13' = docent, '14' = professor, etc.
     */
    @Column(name = "_academic_rank", length = 32)
    private String academicRank;

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

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", university='" + university + '\'' +
                ", academicDegree='" + academicDegree + '\'' +
                ", academicRank='" + academicRank + '\'' +
                '}';
    }
}
