package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Scientific Publication Registry Detail DTO - single-record detail (row fields + extra columns).
 *
 * <p>READ-ONLY. {@code _translations} is never selected.</p>
 */
@Schema(name = "PublicationRegistryDetail", description = "Scientific publication detail (row fields + extra columns)")
public record PublicationDetailDto(
    String id,
    String name,
    String authors,
    Integer authorCounts,
    String sourceName,
    Integer issueYear,
    String universityCode,
    String universityName,
    String publicationTypeCode,
    String publicationTypeName,
    String doi,
    Boolean active,
    String keywords,
    String parameter,
    String publicationDatabaseCode,
    String publicationDatabaseName,
    String educationYear,
    Boolean isChecked
) {}
