package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * University Speciality Registry Detail DTO - detail-drawer payload.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_university_speciality} (NO {@code delete_ts}).</p>
 */
@Schema(name = "UniversitySpecialityRegistryDetail", description = "Full university speciality detail for the read-only detail drawer")
public record SpecialityDetailDto(
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
