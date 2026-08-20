package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bino lifecycle event log — o'zgarmas (immutable) tarix jurnali.
 *
 * <p><b>Maqsad:</b> Bino tarixini saqlash (qurildi, ta'mirlandi, yiqildi, ...)
 * Hozirgi {@code university_building.last_renovation_date} faqat OXIRGI ta'mirni
 * saqlaydi — 2026 ta'miri 2030'dagi yangisiga almashtiriladi, tarix yo'qoladi.
 * Bu jadval har ta'mirni alohida yozib boradi.</p>
 *
 * <p><b>Immutable pattern:</b> Faqat {@code @CreatedDate, @CreatedBy} — UPDATE/DELETE
 * ruxsat etilmaydi (audit integrity). Xato yozuv — alohida "correction" event bilan.</p>
 *
 * <p><b>Avtomatik populatsiya:</b> {@code UniversityBuilding.lastRenovationDate}
 * yangilansa, service qatlamida {@code RENOVATED} event avtomatik yoziladi
 * ({@code BuildingLifecycleListener} orqali).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "building_lifecycle")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingLifecycle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "building_id", nullable = false, updatable = false)
    private UUID buildingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", referencedColumnName = "id",
                insertable = false, updatable = false)
    private UniversityBuilding building;

    /**
     * Event turi — DB CHECK constraint bilan sinxron.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30, updatable = false)
    private EventType eventType;

    @Column(name = "event_date", nullable = false, updatable = false)
    private LocalDate eventDate;

    // =====================================================
    // Moliyaviy ma'lumot
    // =====================================================
    @Column(precision = 15, scale = 2, updatable = false)
    private BigDecimal cost;

    @Column(name = "decree_number", length = 100, updatable = false)
    private String decreeNumber;

    @Column(name = "decree_date", updatable = false)
    private LocalDate decreeDate;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String note;

    // =====================================================
    // Immutable audit (faqat create)
    // =====================================================
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    /**
     * Event turlari — DB CHECK constraint bilan sinxron saqlanadi.
     */
    public enum EventType {
        CONSTRUCTED,     // Qurildi
        RENOVATED,       // Ta'mirlandi
        EXPANDED,        // Kengaytirildi
        REPURPOSED,      // Kategoriya o'zgartirildi
        CLOSED,          // Yopildi (vaqtincha)
        REOPENED,        // Qayta ochildi
        DEMOLISHED       // Yiqib tashlandi
    }
}
