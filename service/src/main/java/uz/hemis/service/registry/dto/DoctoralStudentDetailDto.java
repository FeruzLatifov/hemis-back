package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Doctoral Student (Researcher) Registry Detail DTO - single-record detail.
 *
 * <p>READ-ONLY. Extends the row fields with a few extra columns. PII columns
 * (passport_pin, passport_number, home_address, _translations) are never exposed.</p>
 */
@Schema(name = "DoctoralStudentRegistryDetail", description = "Researcher detail (row fields + extra columns)")
public record DoctoralStudentDetailDto(
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
    Boolean active,
    String dissertationTheme,
    LocalDate birthDate,
    String level,
    String department,
    String paymentForm
) {}
