package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Intellectual Property Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_publication_property} (read via EntityManager
 * native query). {@code _translations} is never selected. The patent-type classifier has no
 * reference table — the raw code is used as the label.</p>
 */
@Schema(name = "PublicationPropertyRegistryRow", description = "Intellectual property row in the flat paginated registry list")
public record PublicationPropertyRowDto(
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
    Boolean active
) {}
