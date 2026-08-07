package uz.hemis.service.classifier.dto;

/**
 * A speciality→OTM attachment row ({@code h_speciality_attachment}).
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
        String specialityId,
        String specialityCode,
        String specialityName,
        String educationType,
        String educationTypeName,
        String educationForm,
        String status
) {
}
