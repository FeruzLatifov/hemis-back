package uz.hemis.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Exam Entity - Mapped to hemishe_e_exam table
 */
@Entity
@Table(name = "hemishe_e_exam")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Exam extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "exam_name", length = 512)
    private String examName;

    @Column(name = "_course")
    private UUID course;

    @Column(name = "_group")
    private UUID group;

    @Column(name = "_teacher")
    private UUID teacher;

    @Column(name = "_university", length = 255)
    private String university;

    @Column(name = "_auditorium")
    private UUID auditorium;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "academic_year", length = 32)
    private String academicYear;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "_exam_type", length = 32)
    private String examType; // MIDTERM, FINAL, RETAKE

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "passing_score")
    private Integer passingScore;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "is_published")
    private Boolean isPublished;

    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    public boolean isPublished() {
        return Boolean.TRUE.equals(isPublished);
    }

    @Override
    public String toString() {
        return "Exam{" +
                "id=" + getId() +
                ", examName='" + examName + '\'' +
                ", course=" + course +
                ", examDate=" + examDate +
                ", examType='" + examType + '\'' +
                '}';
    }
}
