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
 * Schedule Entity - Mapped to hemishe_e_schedule table
 *
 * <p>CRITICAL - Legacy Table Mapping:</p>
 * <ul>
 *   <li>Table: hemishe_e_schedule</li>
 *   <li>Primary Key: id (UUID) - extends BaseEntity</li>
 *   <li>Soft delete: @Where(clause = "delete_ts IS NULL")</li>
 * </ul>
 *
 * <p>Represents class schedule/timetable.</p>
 *
 * @see BaseEntity
 * @since 1.0.0
 */
@Entity
@Table(name = "hemishe_e_schedule")
@Where(clause = "delete_ts IS NULL")
@Getter
@Setter
public class Schedule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // University Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * University code
     * Column: _university VARCHAR(255)
     */
    @Column(name = "_university", length = 255)
    private String university;

    // =====================================================
    // Group Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Group ID
     * Column: _group UUID
     * References: hemishe_e_group.id
     */
    @Column(name = "_group")
    private UUID group;

    // =====================================================
    // Course Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Course ID
     * Column: _course UUID
     * References: hemishe_e_course.id
     */
    @Column(name = "_course")
    private UUID course;

    // =====================================================
    // Teacher Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Teacher ID
     * Column: _teacher UUID
     * References: hemishe_e_teacher.id
     */
    @Column(name = "_teacher")
    private UUID teacher;

    // =====================================================
    // Room Reference (LEGACY FIELD WITH _)
    // =====================================================

    /**
     * Room/Auditorium ID
     * Column: _auditorium UUID
     * References: hemishe_h_auditorium.id
     */
    @Column(name = "_auditorium")
    private UUID auditorium;

    // =====================================================
    // Schedule Time Fields
    // =====================================================

    /**
     * Schedule date
     * Column: schedule_date DATE
     */
    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    /**
     * Start time
     * Column: start_time TIME
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    /**
     * End time
     * Column: end_time TIME
     */
    @Column(name = "end_time")
    private LocalTime endTime;

    /**
     * Day of week
     * Column: day_of_week INTEGER
     * Values: 1=Monday, 2=Tuesday, ..., 7=Sunday
     */
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    /**
     * Pair number (lesson number)
     * Column: pair_number INTEGER
     * Values: 1, 2, 3, 4, 5
     */
    @Column(name = "pair_number")
    private Integer pairNumber;

    // =====================================================
    // Academic Fields
    // =====================================================

    /**
     * Academic year
     * Column: academic_year VARCHAR(32)
     */
    @Column(name = "academic_year", length = 32)
    private String academicYear;

    /**
     * Semester
     * Column: semester INTEGER
     */
    @Column(name = "semester")
    private Integer semester;

    /**
     * Week number
     * Column: week_number INTEGER
     */
    @Column(name = "week_number")
    private Integer weekNumber;

    // =====================================================
    // Classifiers (LEGACY FIELDS WITH _)
    // =====================================================

    /**
     * Lesson type code
     * Column: _lesson_type VARCHAR(32)
     * Examples: 'LECTURE', 'PRACTICE', 'LAB', 'SEMINAR'
     */
    @Column(name = "_lesson_type", length = 32)
    private String lessonType;

    /**
     * Schedule type code
     * Column: _schedule_type VARCHAR(32)
     * Examples: 'REGULAR', 'REPLACEMENT', 'ADDITIONAL'
     */
    @Column(name = "_schedule_type", length = 32)
    private String scheduleType;

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
     * Is cancelled
     * Column: is_cancelled BOOLEAN
     */
    @Column(name = "is_cancelled")
    private Boolean isCancelled;

    // =====================================================
    // Business Methods
    // =====================================================

    public boolean isActive() {
        return Boolean.TRUE.equals(active) && !isDeleted();
    }

    public boolean isCancelled() {
        return Boolean.TRUE.equals(isCancelled);
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + getId() +
                ", university='" + university + '\'' +
                ", group=" + group +
                ", course=" + course +
                ", teacher=" + teacher +
                ", scheduleDate=" + scheduleDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
