package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * University Speciality Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_university_speciality} (entity {@code Specialty};
 * NO {@code delete_ts} column). Read via EntityManager native query.</p>
 */
@Schema(name = "UniversitySpecialityRegistryRow", description = "University speciality row in the flat paginated registry list")
public record SpecialityRowDto(
    String id,
    String universityCode,
    String universityName,
    String specialityCode,
    String specialityName,
    String educationTypeCode,
    String educationTypeName,
    String educationYear,
    String facultyCode,
    Boolean active
) {}
