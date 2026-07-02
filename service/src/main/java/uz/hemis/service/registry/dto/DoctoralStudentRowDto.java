package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Doctoral Student (Researcher) Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_doctorate_student} (read via EntityManager
 * native query). PII columns (passport_pin, passport_number, home_address, _translations)
 * are never exposed; the researcher is shown by full name only.</p>
 */
@Schema(name = "DoctoralStudentRegistryRow", description = "Researcher row in the flat paginated registry list")
public record DoctoralStudentRowDto(
    String id,
    String fullName,
    String studentIdNumber,
    String universityCode,
    String universityName,
    String scienceBranchCode,
    String scienceBranchName,
    String doctoralStudentTypeCode,
    String doctoralStudentTypeName,
    String statusCode,
    String statusName,
    LocalDate acceptedDate,
    Boolean active
) {}
