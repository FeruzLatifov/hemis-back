package uz.hemis.service.classifier.dto;

import java.util.List;

/**
 * Flat list row of the unified speciality classifier ({@code h_speciality}).
 *
 * <p>Powers the paginated curation grid where the frontend filters by
 * {@code educationType} (11=Bakalavr/12=Magistr) and {@code reviewStatus}
 * ({@code NEEDS_REVIEW} = "to'g'rilash kerak") to find and fix rows.</p>
 *
 * @since 2.1.0
 */
public record SpecialityRowDto(
        String id,
        String code,
        String nameUz,
        String nameOz,
        String nameRu,
        String nameEn,
        String educationType,
        String educationTypeName,
        String reviewStatus,
        String parentId,
        Integer hierarchyLevel,
        Boolean active,
        Integer version,
        List<Integer> years
) {
}
