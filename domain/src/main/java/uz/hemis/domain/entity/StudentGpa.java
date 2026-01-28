package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * StudentGpa Entity - Mapped to hemishe_e_student_gpa table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_student_gpa</li>
 *   <li>Primary Key: id (UUID)</li>
 *   <li>Soft delete: NOT applicable (no delete_ts column in this table)</li>
 *   <li>No audit columns (create_ts, update_ts, etc.) in this table</li>
 *   <li>References: student_id → hemishe_e_student.id</li>
 *   <li>References: education_year_code → hemishe_h_education_year.code</li>
 *   <li>References: level_code → hemishe_h_course.code</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * <p>NOTE: This entity does NOT extend BaseEntity because the table
 * lacks standard audit columns (version, create_ts, delete_ts, etc.)</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_student_gpa")
@Getter
@Setter
public class StudentGpa implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key - UUID
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Optimistic locking version
     * Column: version INTEGER NOT NULL
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * Created timestamp
     * Column: create_ts TIMESTAMP
     */
    @Column(name = "create_ts")
    private java.time.LocalDateTime createTs;

    /**
     * Created by user
     * Column: created_by VARCHAR(50)
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * Updated timestamp
     * Column: update_ts TIMESTAMP
     */
    @Column(name = "update_ts")
    private java.time.LocalDateTime updateTs;

    /**
     * Updated by user
     * Column: updated_by VARCHAR(50)
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // =====================================================
    // Student Reference (FK)
    // =====================================================

    /**
     * Student ID
     * Column: student_id UUID NOT NULL
     * References: hemishe_e_student.id
     */
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    // =====================================================
    // Education Year Reference
    // =====================================================

    /**
     * Education year code
     * Column: education_year_code VARCHAR(32)
     * References: hemishe_h_education_year.code
     */
    @Column(name = "education_year_code", length = 32)
    private String educationYearCode;

    // =====================================================
    // GPA Data
    // =====================================================

    /**
     * GPA value
     * Column: gpa VARCHAR(255)
     * Example: "4.0", "3.75"
     */
    @Column(name = "gpa", length = 255)
    private String gpa;

    /**
     * Calculation method
     * Column: method_ VARCHAR(255)
     * Values: "one_year", "all_year"
     */
    @Column(name = "method_", length = 255)
    private String method;

    /**
     * Course level code
     * Column: level_code VARCHAR(32)
     * References: hemishe_h_course.code
     * Example: "12" = 2-kurs
     */
    @Column(name = "level_code", length = 32)
    private String levelCode;

    /**
     * Total credit sum
     * Column: credit_sum VARCHAR(255)
     * Example: "47.0", "60.0"
     */
    @Column(name = "credit_sum", length = 255)
    private String creditSum;

    /**
     * Total subjects count
     * Column: subjects INTEGER
     */
    @Column(name = "subjects")
    private Integer subjects;

    /**
     * Debt subjects count (failed/incomplete)
     * Column: debt_subjects INTEGER
     */
    @Column(name = "debt_subjects")
    private Integer debtSubjects;

    @Override
    public String toString() {
        return "StudentGpa{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", gpa='" + gpa + '\'' +
                ", method='" + method + '\'' +
                ", levelCode='" + levelCode + '\'' +
                ", educationYearCode='" + educationYearCode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentGpa that = (StudentGpa) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
