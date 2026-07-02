package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Dissertation Defense Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_dissertation_defense} (read via EntityManager
 * native query). Owner student resolved by full name only (PII-safe); university resolved
 * indirectly via the doctorate student. {@code _translations} is never selected.</p>
 */
@Schema(name = "DissertationDefenseRegistryRow", description = "Dissertation defense row in the flat paginated registry list")
public record DissertationDefenseRowDto(
    String id,
    String doctorateStudentId,
    String studentName,
    String universityCode,
    String universityName,
    String specialityCode,
    LocalDate defenseDate,
    String diplomaNumber,
    String registerNumber,
    LocalDate approvedDate,
    Boolean active
) {}
