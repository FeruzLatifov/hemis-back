package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Dissertation Defense Registry Detail DTO - detail-drawer payload.
 *
 * <p>READ-ONLY. All {@link DissertationDefenseRowDto} fields plus a few extra columns.
 * {@code _translations} is never selected.</p>
 */
@Schema(name = "DissertationDefenseRegistryDetail", description = "Full dissertation defense detail for the read-only detail drawer")
public record DissertationDefenseDetailDto(
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
    Boolean active,
    String defensePlace,
    LocalDate diplomaGivenDate,
    String diplomaGivenByWhom
) {}
