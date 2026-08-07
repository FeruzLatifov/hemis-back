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
 * Manual-add payload for a new speciality classifier row ({@code h_speciality}).
 *
 * <p>Used by {@code POST /} to let a ministry admin add a speciality by hand. The
 * new row is born {@code NEEDS_REVIEW} (set server-side, not here) so it is not
 * distributed to the 224 OTMs until an admin promotes it via {@code PUT /{id}}.</p>
 *
 * <p>Placement in the tree is by {@code parentId}: {@code null} = a top-level (level 1)
 * node, otherwise a child whose {@code hierarchyLevel} is derived server-side as
 * {@code parent.hierarchyLevel + 1}. Unlike {@link SpecialityUpdateDto}, {@code educationType}
 * is required (a new row must declare its bachelor/master lineage — code '11'/'12').</p>
 *
 * @since 2.1.0
 */
public record SpecialityCreateDto(

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
        @NotBlank(message = "educationType is required")
        @Pattern(regexp = "11|12", message = "educationType must be '11' (Bakalavr) or '12' (Magistr)")
        String educationType,

        /** Parent speciality id; {@code null} creates a top-level (level 1) node. */
        UUID parentId,

        /** Admission years are mandatory: every speciality is year-versioned, so a new row
         *  must declare at least one edition year (mirrors the required Years field in the UI). */
        @NotEmpty(message = "at least one year is required")
        List<@Min(value = 1900, message = "year must be >= 1900")
             @Max(value = 2100, message = "year must be <= 2100") Integer> years
) {
}
