package uz.hemis.service.classifier.dto;

import java.util.List;

/**
 * FLAT v1 distribution item for the unified speciality classifier ({@code h_speciality}).
 *
 * <p>The wire shape shared by BOTH distribution channels so they stay byte-consistent:</p>
 * <ul>
 *   <li>the {@code api-university} bootstrap PULL ({@code GET /api/v1/university/classifiers/speciality}), and</li>
 *   <li>the modern PUSH delta ({@code aggregate_type="classifier"} → webhook fanout {@code data.item}).</li>
 * </ul>
 *
 * <p>Only APPROVED, code-bearing rows are ever emitted (the 53 {@code NEEDS_REVIEW}, incl. the
 * 15 code-less, are excluded). {@code educationType} + {@code educationTypeName} carry the
 * bachelor/master ('11'/'12') discriminator so the OTM side keeps them distinguishable.</p>
 *
 * @since 2.1.0
 */
public record SpecialityDistItemDto(
        String id,
        String code,
        String nameUz,
        String nameOz,
        String nameRu,
        String nameEn,
        String educationType,
        String educationTypeName,
        String parentId,
        Integer hierarchyLevel,
        List<Integer> years,
        Boolean active,
        Boolean isChecked
) {
}
