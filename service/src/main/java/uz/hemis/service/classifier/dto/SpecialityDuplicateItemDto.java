package uz.hemis.service.classifier.dto;

import java.util.List;

/**
 * One existing {@code h_speciality} row that matches a create form's entered code or name —
 * an element of the advisory {@link SpecialityDuplicateCheckDto} warning (never blocks a create).
 *
 * @since 2.1.0
 */
public record SpecialityDuplicateItemDto(
        String id,
        String code,
        String nameUz,
        String educationType,
        String educationTypeName,
        String reviewStatus,
        Integer hierarchyLevel,
        /** Admission years attached to this row (newest first), so the admin sees which editions it covers. */
        List<Integer> years,
        /** The row's code equals the entered code. */
        boolean codeMatch,
        /** The row's folded name equals the entered (folded) name. */
        boolean nameMatch,
        /** The row sits under the same parent the user picked — a sibling collision (strong signal). */
        boolean sameParent
) {
}
