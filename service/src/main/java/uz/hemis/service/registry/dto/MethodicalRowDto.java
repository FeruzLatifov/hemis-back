package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Methodical Publication Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_publication_methodical} (read via
 * EntityManager native query). {@code _translations} is never selected.</p>
 */
@Schema(name = "MethodicalRegistryRow", description = "Methodical publication row in the flat paginated registry list")
public record MethodicalRowDto(
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
    Boolean active
) {}
