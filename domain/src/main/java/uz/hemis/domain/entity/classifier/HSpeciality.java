package uz.hemis.domain.entity.classifier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import uz.hemis.domain.entity.base.AuditableEntity;

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
 * <p><strong>Soft delete</strong> ({@link AuditableEntity} + {@code @SQLRestriction},
 * M013): a classifier row is NEVER removed physically — 224 OTMs and the legacy
 * student rows reference it by UUID, so a lost row is unrecoverable damage. Delete
 * stamps {@code deleted_at}/{@code deleted_by} and the restriction hides the row from
 * every JPQL read (grid, tree, years, duplicates, twins, children guard, distribution
 * pull) including the inherited {@code findById}, so a deleted row 404s on GET/PUT/DELETE.
 * <strong>Native SQL is NOT filtered</strong> — a hand-written predicate is required there
 * (see {@code LegacySpecialitySyncService}). This is distinct from {@code active=false},
 * which is a VISIBLE "retired" state that stays in the lists and pushes a retraction to
 * the OTMs.</p>
 *
 * <p><strong>{@code name}</strong> is a DB-generated column
 * ({@code GENERATED ALWAYS AS (name_uz) STORED}) kept for display parity (mirrors
 * {@code name_uz}); it is read-only from JPA. Distribution is via modern PUSH
 * ({@code aggregate_type="classifier"} → webhook fanout) + the {@code api-university}
 * bootstrap pull — the frozen legacy pull / {@code OLD_CLASSIFIER_MAP} is NOT extended.</p>
 *
 * @see AuditableEntity
 * @since 2.1.0
 */
@Entity
@Table(name = "h_speciality")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class HSpeciality extends AuditableEntity {

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

    /**
     * Parent speciality. NULL = root node.
     *
     * <p>{@code @JsonIgnore}: the audit aspect snapshots a row by serializing the entity, and a LAZY
     * association turns that into a LazyInitializationException the aspect swallows — the before/after
     * images then come back empty and "what did this row look like" is unanswerable. The tree is
     * carried by DTOs (parentId), never by serializing the entity, so nothing else loses anything.</p>
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_h_speciality_parent"))
    private HSpeciality parent;

    /** Child specialities. No cascade REMOVE / orphanRemoval — classifier rows are deactivated, not deleted. */
    @JsonIgnore   // same reason as `parent`: an audit snapshot must not walk the subtree
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

    /**
     * Whether this row is shipped to the 224 OTMs: {@code APPROVED}, code-bearing and active.
     *
     * <p>The single definition of "the OTMs know about this speciality". The distribution snapshot
     * (pull), the modern PUSH fanout and the OTM-attachment guard all read it, so those three can
     * never drift apart — an attachment to a row no OTM has received would hand out a speciality id
     * no Univer can resolve. Mirrors the SQL predicate in
     * {@code HSpecialityRepository.findAllForDistribution}.</p>
     */
    public boolean isDistributable() {
        return reviewStatus == ReviewStatus.APPROVED
                && code != null
                && Boolean.TRUE.equals(active)
                // M013: a soft-deleted row must never be pushed as an UPDATE. The PULL side is
                // already covered by @SQLRestriction; this closes the PUSH side, which reads a
                // still-managed instance inside the deleting transaction.
                && !isDeleted();
    }
}
