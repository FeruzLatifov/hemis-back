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
 * Attendance Entity - Mapped to hemishe_e_attendance table
 */
@Entity
@Table(name = "hemishe_e_attendance")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Attendance extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "_student")
    private UUID student;

    @Column(name = "_course")
    private UUID course;

    @Column(name = "_schedule")
    private UUID schedule;

    @Column(name = "_university", length = 255)
    private String university;

    @Column(name = "_group")
    private UUID group;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "_attendance_type", length = 32)
    private String attendanceType; // PRESENT, ABSENT, LATE, EXCUSED

    @Column(name = "academic_year", length = 32)
    private String academicYear;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "week_number")
    private Integer weekNumber;

    @Column(name = "is_present")
    private Boolean isPresent;

    @Column(name = "reason", length = 1024)
    private String reason;

    public boolean isPresent() {
        return Boolean.TRUE.equals(isPresent);
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + getId() +
                ", student=" + student +
                ", course=" + course +
                ", attendanceDate=" + attendanceDate +
                ", isPresent=" + isPresent +
                '}';
    }
}
