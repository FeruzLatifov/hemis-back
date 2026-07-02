package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Research Activity Registry Detail DTO - detail-drawer payload.
 *
 * <p>READ-ONLY. Same fields as {@link ResearchActivityRowDto}.</p>
 */
@Schema(name = "ResearchActivityRegistryDetail", description = "Full research activity detail for the read-only detail drawer")
public record ResearchActivityDetailDto(
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
