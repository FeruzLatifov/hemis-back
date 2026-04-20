package uz.hemis.domain.entity.university;

import uz.hemis.domain.entity.base.BaseEntity;
import uz.hemis.domain.entity.employee.Teacher;
import uz.hemis.domain.entity.reference.Ownership;
import uz.hemis.domain.entity.reference.Terrain;
import uz.hemis.domain.entity.student.Student;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * University Entity - Mapped to hemishe_e_university table
 *
 * <p><strong>SPECIAL CASE - VARCHAR Primary Key:</strong></p>
 * <ul>
 *   <li>Primary Key: code (VARCHAR 255) - NOT UUID!</li>
 *   <li>All Student/Teacher entities reference this via VARCHAR code</li>
 *   <li>Cannot extend BaseEntity (different PK type)</li>
 *   <li>Audit columns defined manually</li>
 * </ul>
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_university</li>
 *   <li>PK: code VARCHAR(255)</li>
 *   <li>Business Key: tin VARCHAR(255)</li>
 *   <li>Soft delete: @SQLRestriction("delete_ts IS NULL")</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_university")
@SQLRestriction("delete_ts IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class University implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key (VARCHAR - SPECIAL CASE)
    // =====================================================

    /**
     * University code - Primary Key
     * Column: code VARCHAR(255) NOT NULL
     *
     * <p>CRITICAL: This is VARCHAR, not UUID!</p>
     * <p>All references to university use this code.</p>
     */
    @Id
    @Column(name = "code", nullable = false, length = 255)
    private String code;

    // =====================================================
    // Audit Columns (CUBA Pattern — Spring Data JPA Auditing)
    // =====================================================

    /**
     * Version for optimistic locking — managed by Hibernate via {@link Version}.
     * Column: version INTEGER NOT NULL
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * Creation timestamp
     * Column: create_ts TIMESTAMP(6)
     */
    @CreatedDate
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createTs;

    /**
     * Creator username
     * Column: created_by VARCHAR(50)
     */
    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    /**
     * Last update timestamp
     * Column: update_ts TIMESTAMP(6)
     */
    @LastModifiedDate
    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    /**
     * Last updater username
     * Column: updated_by VARCHAR(50)
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * Soft delete timestamp
     * Column: delete_ts TIMESTAMP(6)
     */
    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    /**
     * Soft delete performer
     * Column: deleted_by VARCHAR(50)
     */
    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * TIN - Tax Identification Number
     * Column: tin VARCHAR(255)
     */
    @Column(name = "tin", length = 255)
    private String tin;

    /**
     * University name
     * Column: name VARCHAR(1024) NOT NULL
     */
    @Column(name = "name", nullable = false, length = 1024)
    private String name;

    /**
     * Address
     * Column: address TEXT
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * Cadastre information
     * Column: cadastre TEXT
     */
    @Column(name = "cadastre", columnDefinition = "TEXT")
    private String cadastre;

    /**
     * University URL
     * Column: university_url TEXT
     */
    @Column(name = "university_url", columnDefinition = "TEXT")
    private String universityUrl;

    /**
     * Student portal URL
     * Column: student_url TEXT
     */
    @Column(name = "student_url", columnDefinition = "TEXT")
    private String studentUrl;

    /**
     * Teacher portal URL
     * Column: teacher_url TEXT
     */
    @Column(name = "teacher_url", columnDefinition = "TEXT")
    private String teacherUrl;

    /**
     * UZBMB URL
     * Column: uzbmb_url TEXT
     */
    @Column(name = "uzbmb_url", columnDefinition = "TEXT")
    private String uzbmbUrl;

    // =====================================================
    // Location Classifiers
    // =====================================================

    /**
     * SOATO code (location)
     * Column: _soato VARCHAR(20)
     */
    @Column(name = "_soato", length = 20)
    private String soato;

    /**
     * SOATO region code
     * Column: _soato_region VARCHAR(20)
     */
    @Column(name = "_soato_region", length = 20)
    private String soatoRegion;

    // =====================================================
    // University Classifiers
    // =====================================================

    /**
     * University type code
     * Column: _university_type VARCHAR(32)
     * References: hemishe_h_university_type.code
     */
    @Column(name = "_university_type", length = 32)
    private String universityType;

    /**
     * Ownership type code
     * Column: _ownership VARCHAR(32)
     * References: hemishe_h_ownership.code
     */
    @Column(name = "_ownership", length = 32)
    private String ownership;

    /**
     * University version code
     * Column: _university_version VARCHAR(32)
     */
    @Column(name = "_university_version", length = 32)
    private String universityVersion;

    /**
     * University activity status code
     * Column: _university_activity_status VARCHAR(32)
     */
    @Column(name = "_university_activity_status", length = 32)
    private String universityActivityStatus;

    /**
     * University belongs to code
     * Column: _university_belongs_to VARCHAR(32)
     */
    @Column(name = "_university_belongs_to", length = 32)
    private String universityBelongsTo;

    /**
     * University contract category code
     * Column: _university_contract_category VARCHAR(32)
     */
    @Column(name = "_university_contract_category", length = 32)
    private String universityContractCategory;

    /**
     * Parent university code (self-reference)
     * Column: _parent_university VARCHAR(255)
     * References: hemishe_e_university.code (self)
     */
    @Column(name = "_parent_university", length = 255)
    private String parentUniversity;

    // =====================================================
    // Boolean Flags
    // =====================================================

    /**
     * Active flag
     * Column: active BOOLEAN
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * GPA edit allowed flag
     * Column: gpa_edit BOOLEAN DEFAULT false
     */
    @Column(name = "gpa_edit")
    private Boolean gpaEdit;

    /**
     * Accreditation edit allowed flag
     * Column: accreditation_edit BOOLEAN DEFAULT true
     */
    @Column(name = "accreditation_edit")
    private Boolean accreditationEdit;

    /**
     * Add student allowed flag
     * Column: add_student BOOLEAN DEFAULT false
     */
    @Column(name = "add_student")
    private Boolean addStudent;

    /**
     * Allow grouping flag
     * Column: allow_grouping BOOLEAN
     */
    @Column(name = "allow_grouping")
    private Boolean allowGrouping;

    /**
     * Allow transfer outside flag
     * Column: allow_transfer_outside BOOLEAN
     */
    @Column(name = "allow_transfer_outside")
    private Boolean allowTransferOutside;

    /**
     * OneID integration flag
     * Column: one_id BOOLEAN
     */
    @Column(name = "one_id")
    private Boolean oneId;

    /**
     * Grading system flag
     * Column: grading_system BOOLEAN
     */
    @Column(name = "grading_system")
    private Boolean gradingSystem;

    /**
     * Add foreign student flag
     * Column: add_foreign_student BOOLEAN
     */
    @Column(name = "add_foreign_student")
    private Boolean addForeignStudent;

    /**
     * Terrain code (mahalla)
     * Column: _terrain VARCHAR(32)
     */
    @Column(name = "_terrain", length = 32)
    private String terrain;

    /**
     * Add transfer student flag
     * Column: add_transfer_student BOOLEAN
     */
    @Column(name = "add_transfer_student")
    private Boolean addTransferStudent;

    /**
     * Add academic mobile student flag
     * Column: add_academic_mobile_student BOOLEAN
     */
    @Column(name = "add_academic_mobile_student")
    private Boolean addAcademicMobileStudent;

    // NOTE: _version_type column exists in DB but NOT mapped to entity
    // Reason: CUBA legacy field, not used in new system
    // Hibernate would include it in SELECT even with insertable=false, updatable=false
    // Solution: Don't map it at all - database column is ignored

    /**
     * Mail address
     * Column: mail_address TEXT
     */
    @Column(name = "mail_address", columnDefinition = "TEXT")
    private String mailAddress;

    /**
     * Bank information
     * Column: bank_info TEXT
     */
    @Column(name = "bank_info", columnDefinition = "TEXT")
    private String bankInfo;

    /**
     * Accreditation information
     * Column: accreditation_info TEXT
     */
    @Column(name = "accreditation_info", columnDefinition = "TEXT")
    private String accreditationInfo;

    // =====================================================
    // Business Methods
    // =====================================================

    /**
     * Check if university is deleted
     *
     * @return true if deleteTs is not null
     */
    public boolean isDeleted() {
        return deleteTs != null;
    }

    /**
     * Check if university is active
     *
     * @return true if active flag is true AND not deleted
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    // =====================================================
    // Equals/HashCode based on code (PK)
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof University that)) return false;
        return code != null && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return "University{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
