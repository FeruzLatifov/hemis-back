package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Scientific Project Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_project} (read via EntityManager native query).</p>
 */
@Schema(name = "ProjectRegistryRow", description = "Scientific project row in the flat paginated registry list")
public record ProjectRowDto(
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
    Boolean active
) {}
