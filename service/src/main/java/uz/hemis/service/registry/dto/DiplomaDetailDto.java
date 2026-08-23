package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Diploma Registry Detail DTO - detail-drawer payload for a single diploma.
 *
 * <p>READ-ONLY. All {@link DiplomaRowDto} fields plus a few extra columns. Large
 * {@code @Lob} columns ({@code academic_record}, {@code translations}) are deliberately
 * excluded. PINFL is never exposed.</p>
 */
@Schema(name = "DiplomaRegistryDetail", description = "Full diploma detail for the read-only detail drawer")
public record DiplomaDetailDto(

    @Schema(description = "Diploma id (UUID primary key)", requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Diploma number")
    String diplomaNumber,

    @Schema(description = "Register number")
    String registerNumber,

    @Schema(description = "Register date")
    LocalDate registerDate,

    @Schema(description = "Student id (UUID)")
    String studentId,

    @Schema(description = "Student full name")
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
    Boolean active,

    @Schema(description = "Education type classifier code")
    String educationType,

    @Schema(description = "Admission year classifier code")
    String admissionYear,

    @Schema(description = "Speciality code")
    String specialityCode,

    @Schema(description = "Total credit")
    String totalCredit,

    @Schema(description = "Diploma content hash")
    String hash
) {}
