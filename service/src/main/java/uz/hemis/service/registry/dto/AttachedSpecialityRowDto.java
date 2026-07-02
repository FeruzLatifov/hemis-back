package uz.hemis.service.registry.dto;

/**
 * Attached-speciality list row (University specialities registry).
 *
 * <p>Flat, human-readable projection: every classifier {@code code} is paired with
 * its resolved {@code *Name} (university / education type / education form / speciality).</p>
 *
 * @since 2.0.0
 */
public record AttachedSpecialityRowDto(
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
        Boolean active
) {
}
