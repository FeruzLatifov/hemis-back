package uz.hemis.service.classifier.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Edit + promote payload for a speciality classifier row ({@code h_speciality}).
 *
 * <p>Used by {@code PUT /{id}} to curate the 53 {@code NEEDS_REVIEW} rows: fix
 * the {@code code}/name/level/years and set {@code reviewStatus=APPROVED} to
 * promote. Only mutable curation fields are exposed — the {@code parent_id} tree
 * stays stable (Phase-2 concern).</p>
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

        /** Admission years are mandatory: an edit must keep at least one edition year — saving an
         *  empty set is rejected (422) instead of silently wiping the row's years via replaceYears. */
        @NotEmpty(message = "at least one year is required")
        List<@Min(value = 1900, message = "year must be >= 1900")
             @Max(value = 2100, message = "year must be <= 2100") Integer> years
) {
}
