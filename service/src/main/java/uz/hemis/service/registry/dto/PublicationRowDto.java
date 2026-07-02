package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Scientific Publication Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_publication_scientific} (read via
 * EntityManager native query). {@code _translations} is never selected.</p>
 */
@Schema(name = "PublicationRegistryRow", description = "Scientific publication row in the flat paginated registry list")
public record PublicationRowDto(
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
    Boolean active
) {}
