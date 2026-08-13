package uz.hemis.service.classifier.dto;

/**
 * A speciality→OTM attachment row ({@code university_speciality_attachment}).
 *
 * <p>Flat projection: the attachment fields plus the resolved speciality
 * {@code code}/{@code name}/{@code level} (batch-loaded from {@code h_speciality}
 * to avoid N+1).</p>
 *
 * @since 2.1.0
 */
public record SpecialityAttachmentRowDto(
        String id,
        String universityCode,
        String universityName,
        String specialityId,
        String specialityCode,
        String specialityName,
        /** h_speciality taxonomy depth: 3 = Yo'nalish (direction), 4 = Ichki yo'nalish (sub-direction). */
        Integer hierarchyLevel,
        /** Name of the parent speciality (the direction a sub-direction belongs to); null for a root/direction. */
        String parentName,
        String educationType,
        String educationTypeName,
        String educationForm,
        String educationFormName,
        /** Academic year of the assignment (2026 = 2026-2027) — the attachment's own year. */
        Integer eduYear,
        String status
) {
}
