package uz.hemis.service.registry.dto;

import java.time.LocalDateTime;

/**
 * Attached-speciality detail — all {@link AttachedSpecialityRowDto} fields plus
 * CUBA audit metadata (mirrors {@code FacultyDetailDto}).
 *
 * @since 2.0.0
 */
public record AttachedSpecialityDetailDto(
        String id,
        String universityCode,
        String universityName,
        String educationType,
        String educationTypeName,
        String educationForm,
        String educationFormName,
        String specialityLevel,
        String specialityId,
        String specialityName,
        Boolean active,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        Integer version
) {
}
