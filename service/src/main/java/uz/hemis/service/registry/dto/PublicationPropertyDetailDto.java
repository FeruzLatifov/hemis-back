package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Intellectual Property Registry Detail DTO - detail-drawer payload.
 *
 * <p>READ-ONLY. All {@link PublicationPropertyRowDto} fields plus a few extra columns.
 * {@code _translations} is never selected.</p>
 */
@Schema(name = "PublicationPropertyRegistryDetail", description = "Full intellectual property detail for the read-only detail drawer")
public record PublicationPropertyDetailDto(
    String id,
    String name,
    String authors,
    Integer authorCounts,
    String universityCode,
    String universityName,
    String patentTypeCode,
    String patentTypeName,
    String numbers,
    LocalDate propertyDate,
    String countryCode,
    Boolean active,
    String parameter,
    String publicationDatabaseCode,
    String publicationDatabaseName,
    String educationYear,
    Boolean isChecked
) {}
