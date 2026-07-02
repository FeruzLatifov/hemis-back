package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Methodical Publication Registry Detail DTO - single-record detail (row fields + extra columns).
 *
 * <p>READ-ONLY. {@code _translations} is never selected.</p>
 */
@Schema(name = "MethodicalRegistryDetail", description = "Methodical publication detail (row fields + extra columns)")
public record MethodicalDetailDto(
    String id,
    String name,
    String authors,
    Integer authorCounts,
    String publisher,
    Integer issueYear,
    String sourceName,
    String universityCode,
    String universityName,
    String methodicalTypeCode,
    String methodicalTypeName,
    Boolean active,
    String parameter,
    String publicationDatabaseCode,
    String publicationDatabaseName,
    String educationYear,
    Boolean isChecked
) {}
