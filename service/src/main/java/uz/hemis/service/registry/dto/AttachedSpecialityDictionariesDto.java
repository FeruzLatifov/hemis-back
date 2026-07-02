package uz.hemis.service.registry.dto;

import java.util.List;
import java.util.Map;

/**
 * Filter/form dictionaries for the attached-speciality card (cached).
 *
 * <ul>
 *   <li>{@code universities} / {@code educationTypes} / {@code educationForms} — code/name pairs</li>
 *   <li>{@code specialities} — keyed by {@link SpecialityLevel} name
 *       (BACHELOR / MASTER / ORDINATURA / DOCTORAL) → id/name pairs</li>
 * </ul>
 *
 * @since 2.0.0
 */
public record AttachedSpecialityDictionariesDto(
        List<CodeName> universities,
        List<CodeName> educationTypes,
        List<CodeName> educationForms,
        Map<String, List<IdName>> specialities
) {
    /** Classifier {@code code} → display {@code name}. */
    public record CodeName(String code, String name) {
    }

    /** Speciality {@code id} (UUID string) → display {@code name}. */
    public record IdName(String id, String name) {
    }
}
