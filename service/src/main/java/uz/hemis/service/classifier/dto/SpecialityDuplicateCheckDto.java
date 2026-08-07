package uz.hemis.service.classifier.dto;

import java.util.List;

/**
 * Advisory "already exists" result for the manual add form. Code is intentionally NON-unique in
 * {@code h_speciality}, so this NEVER blocks a create — it just tells the admin what already exists
 * (by exact code and/or folded name) so they can decide.
 *
 * @since 2.1.0
 */
public record SpecialityDuplicateCheckDto(
        boolean codeExists,
        boolean nameExists,
        /**
         * An active row with the SAME education level, SAME code and SAME folded name already exists
         * (parent-independent, null-code aware) — a literal twin. The form blocks the create on this
         * (the create endpoint enforces the same rule); code-only or name-only matches stay advisory.
         */
        boolean exactDuplicate,
        List<SpecialityDuplicateItemDto> matches
) {
}
