package uz.hemis.service.classifier.dto;

/**
 * One OTM-facing speciality-attachment snapshot item — "a speciality this OTM is
 * allowed to run, in this education form".
 *
 * <p>Consumed by the 224 Univer backends over the bootstrap PULL channel
 * ({@code GET /api/v1/university/speciality-attachments}). The wire join key is the
 * natural {@code specialityCode} (not the internal attachment UUID), mirroring
 * {@code SpecialityDistItemDto}; the OTM already knows its own {@code universityCode}
 * (JWT claim), so it is not repeated per row.</p>
 *
 * @since 2.1.0
 */
public record SpecialityAttachmentSnapshotDto(
        String specialityId,
        String specialityCode,
        String specialityName,
        String educationType,
        String educationTypeName,
        String educationForm,
        String status
) {
}
