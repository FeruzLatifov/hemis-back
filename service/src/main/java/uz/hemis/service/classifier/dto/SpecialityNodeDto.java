package uz.hemis.service.classifier.dto;

import java.util.List;

/**
 * Tree node of the unified speciality classifier ({@code h_speciality}).
 *
 * <p>Ministry-side hierarchical projection: each node carries its normalized
 * {@code years} and its child specialities (self-referencing {@code parent_id}
 * tree). Used by the {@code GET /tree} and {@code GET /{id}} endpoints.</p>
 *
 * @since 2.1.0
 */
public record SpecialityNodeDto(
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
        Boolean isChecked,
        List<Integer> years,
        List<SpecialityNodeDto> children
) {
}
