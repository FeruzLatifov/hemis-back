package uz.hemis.domain.entity.infrastructure;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;
import uz.hemis.domain.entity.university.University;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Universitet binosi va inshooti — akademik/operatsional ko'rinish.
 * Excel template: docs/Бино ва иншоотлар жадвали.xlsx (14 ustun)
 *
 * <p><b>Kadastr ma'lumotlari:</b> {@link #cadNumber} kadastr raqami (string).
 * Cadastre tafsilotlari {@link #cadastre} JSONB ustunida snapshot sifatida saqlanadi
 * (caller-provided JSON, integratsiyadan kelmaydi).</p>
 *
 * <p><b>Dual mapping:</b> Har bir FK uchun ikkita field:
 * <ul>
 *   <li>{@code *Code: String} — write path (backward compat, API)</li>
 *   <li>{@code * : Entity LAZY} — read path (relational access)</li>
 * </ul></p>
 *
 * <p><b>Source tracking:</b> univer_sync (OTM push), manual (ministry admin),
 * excel_import (bulk upload), kadastr_sync (external API).</p>
 *
 * @since 2.0.0
 */
@Entity
@Table(name = "university_building")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityBuilding extends AuditableEntity {

    // =====================================================
    // Bog'lanish: Universitet
    // =====================================================
    @Column(name = "university_code", nullable = false)
    private String universityCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private University university;

    // =====================================================
    // Excel col 2: Binoning nomi
    // =====================================================
    @Column(nullable = false, length = 500)
    private String name;

    // =====================================================
    // Bino turi (markaziy klassifikator FK, dual-mapping) — asosiy tur (h_building_type, V023).
    // =====================================================
    @Column(name = "building_type_code", length = 20)
    private String buildingTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_type_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private HBuildingType buildingType;

    /**
     * Ko'p-tur: bino tegishli BARCHA turlar (junction {@code university_building_type}).
     * {@link #buildingTypeCode} = ASOSIY tur (ro'yxat/filtr); bu — to'liq ro'yxat (o'quv+ma'muriy+...).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "university_building_type",
                     joinColumns = @JoinColumn(name = "building_id"))
    @Column(name = "building_type_code", length = 20)
    @Builder.Default
    private java.util.Set<String> buildingTypeCodes = new java.util.LinkedHashSet<>();

    // =====================================================
    // Egalik shakli (classifier FK, dual-mapping) — OWN/OPERATIVE/RENT (h_building_ownership, V024).
    // =====================================================
    @Column(name = "ownership_code", length = 20)
    private String ownershipCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownership_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private HBuildingOwnership ownership;

    // =====================================================
    // Excel col 3: Manzil (cadastre'dan auto-fill mumkin)
    // =====================================================
    @Column(columnDefinition = "TEXT")
    private String address;

    // =====================================================
    // Excel col 4-6: Qurilish parametrlari
    // =====================================================
    private Integer yearBuilt;
    private Integer capacity;          // o'quv/yotoq o'rin
    private Integer floorCount;

    // =====================================================
    // Excel col 7-8: Maydon (cadastre'dan auto-fill mumkin)
    // =====================================================
    @Column(precision = 10, scale = 2)
    private BigDecimal totalArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal usableArea;

    // =====================================================
    // Excel col 9: Qurilish materiali (classifier FK)
    // =====================================================
    @Column(name = "construction_material_code", length = 20)
    private String constructionMaterialCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "construction_material_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private HConstructionMaterial constructionMaterial;

    // =====================================================
    // Excel col 10: Tom turi (classifier FK)
    // =====================================================
    @Column(name = "roof_type_code", length = 20)
    private String roofTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roof_type_code", referencedColumnName = "code",
                insertable = false, updatable = false)
    private HRoofType roofType;

    // =====================================================
    // Excel col 11: Oxirgi ta'mir (tarixi — BuildingLifecycle'da)
    // =====================================================
    private LocalDate lastRenovationDate;

    // =====================================================
    // Excel col 12: Joylashuv — WGS84 (NULLABLE: sync flexibility)
    // =====================================================
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String mapUrl;

    // =====================================================
    // Excel col 13: Kadastr raqami — university_cadastre(cad_number)'ga FK (M009).
    // Kadastr fakti (egalar/maydon/qiymat) university_cadastre'da; bu yerda faqat bog'lanish.
    // =====================================================
    @Column(name = "cad_number", length = 50)
    private String cadNumber;

    // =====================================================
    // Excel col 14: Izoh
    // =====================================================
    @Column(columnDefinition = "TEXT")
    private String note;

    // =====================================================
    // Sync tracking
    // =====================================================
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String source = "univer_sync";

    @Column(name = "source_uid", length = 255)
    private String sourceUid;

    private LocalDateTime syncedAt;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    /**
     * Bo'sh cad_number → null (SEV-3): FK {@code fk_ub_cadastre} faqat NULL'ni istisno qiladi, "" ni EMAS —
     * "" ni university_cadastre'da qidirib topolmay FK'ni buzardi. ensureCadastreExists ham bo'sh raqamni
     * o'tkazib yuboradi (isBlank), shuning uchun bu yerda entity qiymatini ham normallashtiramiz.
     */
    @PrePersist
    @PreUpdate
    private void normalizeCadNumber() {
        if (cadNumber != null && cadNumber.isBlank()) {
            cadNumber = null;
        }
    }
}
