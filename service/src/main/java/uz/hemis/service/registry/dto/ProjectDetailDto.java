package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Scientific Project Registry Detail DTO - single-record detail (row fields + extra columns).
 *
 * <p>READ-ONLY. {@code _translations} is never selected.</p>
 */
@Schema(name = "ProjectRegistryDetail", description = "Scientific project detail (row fields + extra columns)")
public record ProjectDetailDto(
    String id,
    String name,
    String projectNumber,
    String universityCode,
    String universityName,
    String projectTypeCode,
    String projectTypeName,
    String contractNumber,
    LocalDate contractDate,
    LocalDate startDate,
    LocalDate endDate,
    Boolean active,
    String department,
    String localityCode,
    String localityName,
    String projectCurrencyCode,
    String projectCurrencyName
) {}
