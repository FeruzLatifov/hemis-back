package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Certificate Registry Detail DTO - detail-drawer payload for a single certificate.
 *
 * <p>READ-ONLY. All {@link CertificateRowDto} fields plus the subject classifier
 * (code + resolved label). PINFL is never exposed.</p>
 */
@Schema(name = "CertificateRegistryDetail", description = "Full certificate detail for the read-only detail drawer")
public record CertificateDetailDto(

    @Schema(description = "Certificate id (UUID primary key)", requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Student id (UUID)")
    String studentId,

    @Schema(description = "Student full name")
    String studentName,

    @Schema(description = "University code")
    String universityCode,

    @Schema(description = "University name")
    String universityName,

    @Schema(description = "Certificate type classifier code")
    String certificateType,

    @Schema(description = "Certificate type name (resolved label, falls back to code)")
    String certificateTypeName,

    @Schema(description = "Certificate name classifier code")
    String certificateName,

    @Schema(description = "Certificate name label (resolved label, falls back to code)")
    String certificateNameLabel,

    @Schema(description = "Certificate grade classifier code")
    String certificateGrade,

    @Schema(description = "Certificate grade name (resolved label, falls back to code)")
    String certificateGradeName,

    @Schema(description = "Serial number")
    String serialNumber,

    @Schema(description = "Issue date")
    LocalDate issueDate,

    @Schema(description = "Valid until date")
    LocalDate validDate,

    @Schema(description = "Active status")
    Boolean active,

    @Schema(description = "Certificate subject classifier code")
    String certificateSubject,

    @Schema(description = "Certificate subject name (resolved label, falls back to code)")
    String certificateSubjectName
) {}
