package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Research Activity Registry Row DTO - flat list row.
 *
 * <p>READ-ONLY. Source table: {@code hemishe_e_research_activity} (read via EntityManager
 * native query).</p>
 */
@Schema(name = "ResearchActivityRegistryRow", description = "Research activity row in the flat paginated registry list")
public record ResearchActivityRowDto(
    String id,
    String universityCode,
    String universityName,
    String educationYear,
    String scholarDatabaseCode,
    String scholarDatabaseName,
    String hIndex,
    String scientificWorkCount,
    String referenceCount,
    String link
) {}
