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

    @Schema(description = "Diploma id (UUID primary key)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Diploma number", example = "D-000123")
    String diplomaNumber,

    @Schema(description = "Register number", example = "R-556677")
    String registerNumber,

    @Schema(description = "Register date")
    LocalDate registerDate,

    @Schema(description = "Student id (UUID)", example = "1b2c3d4e-5717-4562-b3fc-2c963f66afa6")
    String studentId,

    @Schema(description = "Student full name (lastname firstname fathername)", example = "Aliyev Ali Valiyevich")
    String studentName,

    @Schema(description = "University code", example = "00001")
    String universityCode,

    @Schema(description = "University name", example = "TATU")
    String universityName,

    @Schema(description = "Speciality name", example = "Kompyuter injiniringi")
    String specialityName,

    @Schema(description = "Education year classifier code", example = "2024")
    String educationYear,

    @Schema(description = "Graduation date")
    LocalDate graduationDate,

    @Schema(description = "Average grade", example = "4.5")
    String avgGrade,

    @Schema(description = "Verify status/hash marker", example = "verified")
    String verify,

    @Schema(description = "Active status", example = "true")
    Boolean active
) {}
