package uz.hemis.service.classifier.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Edit + promote payload for a speciality classifier row ({@code h_speciality}).
 *
 * <p>Used by {@code PUT /{id}} to curate the {@code NEEDS_REVIEW} rows: fix the
 * {@code code}/name/years and set {@code reviewStatus=APPROVED} to promote. Placement
 * mirrors the create form: send {@code hierarchyLevel} (1-4) plus, for a level 2-4 row,
 * the {@code parentId} that sits exactly one level above. A row that still has
 * sub-directions cannot change its own level — the children must be re-placed first
 * ({@code SPECIALITY_HAS_CHILDREN_MOVE_FIRST}, 422). A successful move drops the row to
 * {@code NEEDS_REVIEW} for re-approval in its new place; the backend also rejects a move
 * that would cycle or mismatch the parent's level/type. Omit {@code hierarchyLevel} to
 * leave placement untouched.</p>
 *
 * @since 2.1.0
 */
public record SpecialityUpdateDto(

        @Size(max = 64, message = "code max 64 chars")
        String code,

        @NotBlank(message = "nameUz is required")
        @Size(max = 512)
        String nameUz,

        @Size(max = 512)
        String nameOz,

        @Size(max = 512)
        String nameRu,

        @Size(max = 512)
        String nameEn,

        /** Education type code — FK into {@code hemishe_h_education_type}: '11'=Bakalavr, '12'=Magistr. */
        @Pattern(regexp = "11|12", message = "educationType must be '11' (Bakalavr) or '12' (Magistr)")
        String educationType,

        @Pattern(regexp = "APPROVED|NEEDS_REVIEW", message = "reviewStatus must be APPROVED or NEEDS_REVIEW")
        String reviewStatus,

        /** Target depth (1=Bilim sohasi … 4=Ichki yo'nalish). {@code null} = leave placement unchanged.
         *  When set, {@link #parentId} must be null for level 1, else a same-education-type node one
         *  level above (level {@code hierarchyLevel - 1}). */
        @Min(value = 1, message = "hierarchyLevel must be >= 1")
        @Max(value = 4, message = "hierarchyLevel must be <= 4")
        Integer hierarchyLevel,

        /** New parent for the row (paired with {@link #hierarchyLevel}). Null for a top-level (level 1)
         *  row. Ignored when {@code hierarchyLevel} is null. */
        UUID parentId,

        /** Admission years are mandatory: an edit must keep at least one edition year — saving an
         *  empty set is rejected (422) instead of silently wiping the row's years via replaceYears. */
        @NotEmpty(message = "at least one year is required")
        List<@Min(value = 1900, message = "year must be >= 1900")
             @Max(value = 2100, message = "year must be <= 2100") Integer> years
) {
}
