package uz.hemis.domain.entity.research;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Akademik Davomat Hisobotlari Entity
 *
 * <p>Table: hemishe_r_academic_attendance</p>
 * <p>Primary key: id (UUID)</p>
 *
 * <p>Universitet, fakultet, ta'lim turi, kurs bo'yicha davomat statistikasi</p>
 *
 * <p><strong>OLD-HEMIS Compatible:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_RAcademicAttendance</li>
 *   <li>Table: hemishe_r_academic_attendance</li>
 *   <li>100% backward compatible with CUBA Platform</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_r_academic_attendance")
@SQLRestriction("delete_ts IS NULL")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class RAcademicAttendance implements Serializable, Persistable<UUID> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Universitet kodi
     */
    @Column(name = "university_code")
    private String universityCode;

    /**
     * Universitet nomi
     */
    @Column(name = "university_name", columnDefinition = "TEXT")
    private String universityName;

    /**
     * Fakultet kodi
     */
    @Column(name = "faculty_code")
    private String facultyCode;

    /**
     * Fakultet nomi
     */
    @Column(name = "faculty_name", columnDefinition = "TEXT")
    private String facultyName;

    /**
     * Ta'lim turi kodi
     */
    @Column(name = "education_type_code")
    private String educationTypeCode;

    /**
     * Ta'lim turi nomi
     */
    @Column(name = "education_type_name")
    private String educationTypeName;

    /**
     * O'quv yili kodi
     */
    @Column(name = "education_year_code")
    private String educationYearCode;

    /**
     * O'quv yili nomi
     */
    @Column(name = "education_year_name")
    private String educationYearName;

    /**
     * Semester turi kodi
     */
    @Column(name = "semester_type_code")
    private String semesterTypeCode;

    /**
     * Semester turi nomi
     */
    @Column(name = "semester_type_name")
    private String semesterTypeName;

    /**
     * Kurs kodi
     */
    @Column(name = "course_code")
    private String courseCode;

    /**
     * Kurs nomi
     */
    @Column(name = "course_name")
    private String courseName;

    /**
     * Yangilash sanasi
     */
    @Column(name = "update_date")
    private LocalDate updateDate;

    /**
     * Davomat foizi (0-100)
     */
    @Column(name = "attendance_percent")
    private Double attendancePercent;

    /**
     * Yomon davomatli talabalar soni
     */
    @Column(name = "bad_attendance_student_count")
    private Integer badAttendanceStudentCount;

    // =====================================================
    // CUBA Audit Fields
    // =====================================================

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreatedDate
    @Column(name = "create_ts", updatable = false)
    private LocalDateTime createTs;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "update_ts")
    private LocalDateTime updateTs;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "delete_ts")
    private LocalDateTime deleteTs;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @Transient
    private boolean isNew = true;

    public boolean isDeleted() {
        return deleteTs != null;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    protected void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RAcademicAttendance that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
