package uz.hemis.service.classifier.dto;

import java.time.LocalDateTime;

/**
 * One soft-deleted speciality row ({@code h_speciality}, M013) for the "Deleted specialities" list.
 *
 * <p>Deliberately thinner than {@link SpecialityRowDto}: the list exists to answer "what did we
 * remove, when, by whom — and do I want it back", so it carries the identity fields plus the
 * deletion stamp and nothing else. Years are not loaded; they are still in the DB and come back
 * untouched on restore.</p>
 *
 * @since 2.2.0
 */
public record SpecialityDeletedRowDto(
        String id,
        String code,
        String nameUz,
        String educationType,
        String educationTypeName,
        String reviewStatus,
        LocalDateTime deletedAt,
        String deletedBy
) {
}
