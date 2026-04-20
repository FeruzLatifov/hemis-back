package uz.hemis.domain.entity.finance;

import uz.hemis.domain.entity.academic.Course;
import uz.hemis.domain.entity.academic.Faculty;
import uz.hemis.domain.entity.university.University;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ContractStatistics Entity - Mapped to hemishe_r_contract_statistics table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_r_contract_statistics</li>
 *   <li>Primary Key: id (UUID)</li>
 *   <li>Soft delete: @SQLRestriction("delete_ts IS NULL")</li>
 *   <li>References: _university → hemishe_e_university.code</li>
 *   <li>References: _education_year → hemishe_h_education_year.code</li>
 *   <li>References: _education_type → hemishe_h_education_type.code</li>
 *   <li>References: _education_form → hemishe_h_education_form.code</li>
 *   <li>References: _faculty → hemishe_e_university_department.code</li>
 *   <li>References: _course → hemishe_h_course.code</li>
 *   <li>References: _semester → hemishe_h_semester.code</li>
 * </ul>
 *
 * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_r_contract_statistics")
@SQLRestriction("delete_ts IS NULL")
@Getter
@Setter
public class ContractStatistics implements Serializable {

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
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createTs;

    /**
     * Created by user
     * Column: created_by VARCHAR(50)
     */
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    /**
     * Updated timestamp
     * Column: update_ts TIMESTAMP
     */
    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    /**
     * Updated by user
     * Column: updated_by VARCHAR(50)
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * Soft delete timestamp
     * Column: delete_ts TIMESTAMP
     */
    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    /**
     * Soft delete performer username
     * Column: deleted_by VARCHAR(50)
     */
    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    // =====================================================
    // Business Fields
    // =====================================================

    /**
     * Statistics date
     * Column: _date DATE
     */
    @Column(name = "_date")
    private LocalDate date;

    /**
     * University code
     * Column: _university VARCHAR(255)
     * References: hemishe_e_university.code
     */
    @Column(name = "_university", length = 255)
    private String university;

    /**
     * Education year code
     * Column: _education_year VARCHAR(255)
     * References: hemishe_h_education_year.code
     */
    @Column(name = "_education_year", length = 255)
    private String educationYear;

    /**
     * Education type code
     * Column: _education_type VARCHAR(255)
     * References: hemishe_h_education_type.code
     */
    @Column(name = "_education_type", length = 255)
    private String educationType;

    /**
     * Education form code
     * Column: _education_form VARCHAR(255)
     * References: hemishe_h_education_form.code
     */
    @Column(name = "_education_form", length = 255)
    private String educationForm;

    /**
     * Faculty code
     * Column: _faculty VARCHAR(255)
     * References: hemishe_e_university_department.code
     */
    @Column(name = "_faculty", length = 255)
    private String faculty;

    /**
     * Course code
     * Column: _course VARCHAR(255)
     * References: hemishe_h_course.code
     */
    @Column(name = "_course", length = 255)
    private String course;

    /**
     * Semester code
     * Column: _semester VARCHAR(255)
     * References: hemishe_h_semester.code
     */
    @Column(name = "_semester", length = 255)
    private String semester;

    /**
     * Daily count
     * Column: daily_count INTEGER
     */
    @Column(name = "daily_count")
    private Integer dailyCount;

    /**
     * Total count
     * Column: total INTEGER
     */
    @Column(name = "total")
    private Integer total;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (version == null) {
            version = 1;
        }
        createTs = LocalDateTime.now();
        updateTs = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTs = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractStatistics that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ContractStatistics{" +
                "id=" + id +
                ", university='" + university + '\'' +
                ", educationYear='" + educationYear + '\'' +
                ", date=" + date +
                ", dailyCount=" + dailyCount +
                ", total=" + total +
                '}';
    }
}
