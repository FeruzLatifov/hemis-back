package uz.hemis.service.registry.dto;

import java.time.LocalDateTime;

/**
 * One soft-deleted university row ({@code hemishe_e_university.delete_ts}) for the "Deleted" list.
 *
 * <p>Deliberately thinner than {@link uz.hemis.common.dto.university.UniversityDto}: the list exists
 * to answer "what did we remove, when, by whom — and do I want it back", so it carries the identity
 * fields plus the deletion stamp and nothing else. Everything else stays in the DB and comes back
 * untouched on restore.</p>
 *
 * @since 2.2.0
 */
public record UniversityDeletedRowDto(
        String code,
        String name,
        String tin,
        LocalDateTime deletedAt,
        String deletedBy
) {
}
