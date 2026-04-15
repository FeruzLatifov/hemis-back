package uz.hemis.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * University Lifecycle Event — immutable event log
 *
 * <p><strong>Table:</strong> university_lifecycle (1:N with university)</p>
 *
 * <p>Does NOT extend ModernBaseEntity — no version, no soft delete.
 * Lifecycle events are immutable audit records that never change.</p>
 *
 * <p><strong>Event types:</strong></p>
 * <ul>
 *   <li>CLOSED — yopilgan</li>
 *   <li>MERGED — boshqa universitetga qo'shilgan</li>
 *   <li>SPLIT — bo'lingan</li>
 *   <li>LICENSE_REVOKED — litsenziya bekor qilingan</li>
 *   <li>SUSPENDED — muvaqqat to'xtatilgan</li>
 *   <li>REACTIVATED — qayta faollashtirilgan</li>
 *   <li>RENAMED — nomi o'zgartirilgan</li>
 *   <li>REORGANIZED — qayta tashkil etilgan</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_lifecycle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityLifecycle implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // Primary Key
    // =====================================================

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // =====================================================
    // University Reference
    // =====================================================

    /**
     * University code — plain column, NOT a JPA relationship.
     */
    @Column(name = "university_code", nullable = false)
    private String universityCode;

    // =====================================================
    // Event Classification
    // =====================================================

    /**
     * Event type.
     * CHECK constraint in DB: CLOSED, MERGED, SPLIT, LICENSE_REVOKED,
     * SUSPENDED, REACTIVATED, RENAMED, REORGANIZED
     */
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    /**
     * Date when the event occurred.
     */
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    // =====================================================
    // Successor
    // =====================================================

    /**
     * Successor university code (for MERGED/SPLIT/REORGANIZED events).
     * <p>MERGED: A to B — successor = B</p>
     * <p>SPLIT: A to B,C — 2 records: successor=B and successor=C</p>
     */
    @Column(name = "successor_code")
    private String successorCode;

    // =====================================================
    // Government Decree
    // =====================================================

    @Column(name = "decree_number", length = 100)
    private String decreeNumber;

    @Column(name = "decree_date")
    private LocalDate decreeDate;

    // =====================================================
    // Snapshot at Time of Event
    // =====================================================

    /**
     * Number of students at time of event (historical snapshot).
     */
    @Column(name = "students_count")
    private Integer studentsCount;

    /**
     * Number of employees at time of event (historical snapshot).
     */
    @Column(name = "employees_count")
    private Integer employeesCount;

    // =====================================================
    // Old/New Name (RENAMED events)
    // =====================================================

    @Column(name = "old_name", length = 1024)
    private String oldName;

    @Column(name = "new_name", length = 1024)
    private String newName;

    // =====================================================
    // Notes
    // =====================================================

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // =====================================================
    // Audit (who recorded this event)
    // =====================================================

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    // =====================================================
    // JPA Lifecycle Hooks
    // =====================================================

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // =====================================================
    // Equals & HashCode (based on ID)
    // =====================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniversityLifecycle)) return false;
        UniversityLifecycle that = (UniversityLifecycle) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UniversityLifecycle{" +
                "id=" + id +
                ", universityCode='" + universityCode + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventDate=" + eventDate +
                '}';
    }
}
