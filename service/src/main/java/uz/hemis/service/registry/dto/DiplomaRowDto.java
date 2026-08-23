package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Diploma Registry Row DTO - flat list row for the Diplomas (Diplomlar) registry.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_student_diploma} (double-mapped physical
 * table — read via EntityManager native query, never a JpaRepository). PINFL is never
 * exposed; the student is shown by full name only.</p>
 */
@Schema(name = "DiplomaRegistryRow", description = "Diploma row in the flat paginated registry list")
public record DiplomaRowDto(

    @Schema(description = "Diploma id (UUID primary key)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Diploma number")
    String diplomaNumber,

    @Schema(description = "Register number")
    String registerNumber,

    @Schema(description = "Register date")
    LocalDate registerDate,

    @Schema(description = "Student id (UUID)")
    String studentId,

    @Schema(description = "Student full name (lastname firstname fathername)")
    String studentName,

    @Schema(description = "University code")
    String universityCode,

    @Schema(description = "University name")
    String universityName,

    @Schema(description = "Speciality name")
    String specialityName,

    @Schema(description = "Education year classifier code")
    String educationYear,

    @Schema(description = "Graduation date")
    LocalDate graduationDate,

    @Schema(description = "Average grade")
    String avgGrade,

    @Schema(description = "Verify status/hash marker")
    String verify,

    @Schema(description = "Active status")
    Boolean active
) {}
