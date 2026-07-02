package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Certificate Registry Row DTO - flat list row for the Certificates (Sertifikatlar) registry.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_student_certificate} (native query). Classifier
 * labels are resolved via LEFT JOIN, falling back to the raw code. PINFL is never exposed.</p>
 */
@Schema(name = "CertificateRegistryRow", description = "Certificate row in the flat paginated registry list")
public record CertificateRowDto(

    @Schema(description = "Certificate id (UUID primary key)", requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Student id (UUID)")
    String studentId,

    @Schema(description = "Student full name")
    String studentName,

    @Schema(description = "University code", example = "00001")
    String universityCode,

    @Schema(description = "University name", example = "TATU")
    String universityName,

    @Schema(description = "Certificate type classifier code", example = "11")
    String certificateType,

    @Schema(description = "Certificate type name (resolved label, falls back to code)")
    String certificateTypeName,

    @Schema(description = "Certificate name classifier code", example = "11")
    String certificateName,

    @Schema(description = "Certificate name label (resolved label, falls back to code)")
    String certificateNameLabel,

    @Schema(description = "Certificate grade classifier code", example = "11")
    String certificateGrade,

    @Schema(description = "Certificate grade name (resolved label, falls back to code)")
    String certificateGradeName,

    @Schema(description = "Serial number", example = "AB1234567")
    String serialNumber,

    @Schema(description = "Issue date")
    LocalDate issueDate,

    @Schema(description = "Valid until date")
    LocalDate validDate,

    @Schema(description = "Active status", example = "true")
    Boolean active
) {}
