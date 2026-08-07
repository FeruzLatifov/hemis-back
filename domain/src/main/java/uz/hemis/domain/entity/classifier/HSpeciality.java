package uz.hemis.domain.entity.classifier;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.hemis.domain.entity.base.AuditableEntityNoSoftDelete;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unified speciality classifier — {@code h_speciality} (V018).
 *
 * <p>One merged table for bachelor + master specialities, keyed by UUID (the
 * legacy/xlsx ID), with an {@code education_type} FK discriminator (a code into
 * {@code hemishe_h_education_type}: '11'=Bakalavr, '12'=Magistr), a
 * self-referencing parent tree (copied from {@code Menu}), normalized years
 * ({@link HSpecialityYear}, 1:N), and a {@link ReviewStatus} workflow.</p>
 *
 * <p><strong>No soft delete</strong> ({@link AuditableEntityNoSoftDelete}): a
 * classifier row is deactivated via {@code active=false}, never soft-deleted —
 * so there is no {@code @SQLRestriction}.</p>
 *
 * <p><strong>{@code name}</strong> is a DB-generated column
 * ({@code GENERATED ALWAYS AS (name_uz) STORED}) kept for display parity (mirrors
 * {@code name_uz}); it is read-only from JPA. Distribution is via modern PUSH
 * ({@code aggregate_type="classifier"} → webhook fanout) + the {@code api-university}
 * bootstrap pull — the frozen legacy pull / {@code OLD_CLASSIFIER_MAP} is NOT extended.</p>
 *
 * @see AuditableEntityNoSoftDelete
 * @since 2.1.0
 */
@Entity
@Table(name = "h_speciality")
@Getter
@Setter
public class HSpeciality extends AuditableEntityNoSoftDelete {

    private static final long serialVersionUID = 1L;

    /** Speciality code. NULLABLE: 15 NEEDS_REVIEW rows are code-less until curated. */
    @Column(name = "code", length = 64)
    private String code;

    @Column(name = "name_uz", nullable = false, length = 512)
    private String nameUz;

    /** oz-UZ (Uzbek Cyrillic). NULL where the source had no Cyrillic (xlsx-sourced APPROVED rows). */
    @Column(name = "name_oz", length = 512)
    private String nameOz;

    @Column(name = "name_ru", length = 512)
    private String nameRu;

    @Column(name = "name_en", length = 512)
    private String nameEn;

    /** DB-generated ({@code name_uz}) — Univer-pull display column; read-only from JPA. */
    @Column(name = "name", length = 512, insertable = false, updatable = false)
    private String name;

    /** Identity/search key — DB-generated ({@code h_speciality_fold(name_uz)}, V018); read-only from JPA. */
    @Column(name = "name_search", length = 512, insertable = false, updatable = false)
    private String nameSearch;

    /**
     * Education type — FK code to {@code hemishe_h_education_type.code}
     * ('11'=Bakalavr, '12'=Magistr). The same classifier {@code Student._education_type}
     * references (single source of truth — no standalone enum). Stored as a plain code
     * String (mirrors {@code Student.educationType}, no JPA relationship to avoid N+1);
     * the display label is resolved via {@code EducationTypeRepository} at the service boundary.
     */
    @Column(name = "education_type", nullable = false, length = 32)
    private String educationType;

    @Convert(converter = ReviewStatus.Converter.class)
    @Column(name = "review_status", nullable = false, length = 20)
    private ReviewStatus reviewStatus = ReviewStatus.APPROVED;

    // =====================================================
    // Hierarchical structure (self-reference; copied from Menu)
    // =====================================================

    /** Parent speciality. NULL = root node. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_h_speciality_parent"))
    private HSpeciality parent;

    /** Child specialities. No cascade REMOVE / orphanRemoval — classifier rows are deactivated, not deleted. */
    @OneToMany(mappedBy = "parent",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false)
    private List<HSpeciality> children = new ArrayList<>();

    /** Display-only depth (from the xlsx / derived); the authoritative tree is {@code parent_id}. */
    @Column(name = "hierarchy_level")
    private Integer hierarchyLevel;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_checked", nullable = false)
    private Boolean isChecked = false;

    // =====================================================
    // Helper methods
    // =====================================================

    /** Null-safe parent id (no LAZY init beyond the FK). */
    public UUID getParentId() {
        return parent != null ? parent.getId() : null;
    }

    public boolean isRoot() {
        return parent == null;
    }
}
