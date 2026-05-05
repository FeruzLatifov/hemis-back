package uz.hemis.domain.entity.university;

import jakarta.persistence.*;
import lombok.*;
import uz.hemis.domain.entity.base.ImmutableEntity;
import uz.hemis.domain.entity.enums.LifecycleEventType;

import java.time.LocalDate;

/**
 * University Lifecycle Event — immutable event log
 *
 * <p><strong>Table:</strong> university_lifecycle (1:N with university)</p>
 *
 * <p>Extends ImmutableEntity — no version, no update, no soft delete.
 * Lifecycle events are immutable audit records that never change.</p>
 *
 * <p><strong>Event types:</strong></p>
 * <ul>
 *   <li>CLOSED — yopilgan</li>
 *   <li>MERGED — boshqa universitetga qo'shilgan (successor required)</li>
 *   <li>LICENSE_REVOKED — litsenziya bekor qilingan</li>
 *   <li>SUSPENDED — muvaqqat to'xtatilgan</li>
 *   <li>REACTIVATED — qayta faollashtirilgan</li>
 *   <li>REORGANIZED — qayta tashkil etilgan (successor required)</li>
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
public class UniversityLifecycle extends ImmutableEntity {

    private static final long serialVersionUID = 1L;

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
     * Event type — stored as STRING to match DB CHECK constraint.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private LifecycleEventType eventType;

    /**
     * Date when the event occurred.
     */
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    // =====================================================
    // Successor
    // =====================================================

    /**
     * Successor university code (for MERGED/REORGANIZED events).
     * <p>MERGED: A to B — successor = B</p>
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
    // Notes (free-form description)
    // =====================================================

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Override
    public String toString() {
        return "UniversityLifecycle{" +
                "id=" + getId() +
                ", universityCode='" + universityCode + '\'' +
                ", eventType=" + eventType +
                ", eventDate=" + eventDate +
                '}';
    }
}
