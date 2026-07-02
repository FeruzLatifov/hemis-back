package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Scholarship Registry Row DTO - flat list row for the Scholarships (Stipendiyalar) registry.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_student_scholarship_full} (native query).
 * PINFL is never exposed.</p>
 *
 * <p><strong>Note:</strong> the physical table has no {@code active} column (confirmed vs the
 * CUBA {@code EStudentScholarshipFull} entity), so {@code active} is derived:
 * {@code end_date IS NULL OR end_date >= CURRENT_DATE}.</p>
 */
@Schema(name = "ScholarshipRegistryRow", description = "Scholarship row in the flat paginated registry list")
public record ScholarshipRowDto(

    @Schema(description = "Scholarship id (UUID primary key)", requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Student id (UUID)")
    String studentId,

    @Schema(description = "Student full name")
    String studentName,

    @Schema(description = "University code", example = "00001")
    String universityCode,

    @Schema(description = "University name", example = "TATU")
    String universityName,

    @Schema(description = "Education year classifier code", example = "2024")
    String educationYear,

    @Schema(description = "Semester number", example = "3")
    String semesterNumber,

    @Schema(description = "Stipend category classifier code", example = "11")
    String stipendCategory,

    @Schema(description = "Stipend type classifier code", example = "11")
    String stipendType,

    @Schema(description = "Payment form classifier code", example = "11")
    String paymentForm,

    @Schema(description = "Decree", example = "12/2024")
    String decree,

    @Schema(description = "Start date")
    LocalDate startDate,

    @Schema(description = "End date")
    LocalDate endDate,

    @Schema(description = "Active status (derived from end_date)", example = "true")
    Boolean active
) {}
