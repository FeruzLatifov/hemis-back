package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.UniversityDto;
import uz.hemis.domain.entity.University;

import java.util.List;

/**
 * University Mapper - MapStruct Entity ↔ DTO conversion
 *
 * <p><strong>MapStruct Configuration:</strong></p>
 * <ul>
 *   <li>componentModel = "spring" - generates Spring @Component</li>
 *   <li>unmappedTargetPolicy = WARN - warn about unmapped fields</li>
 *   <li>Auto-generated implementation at compile time</li>
 * </ul>
 *
 * <p><strong>Field Mapping:</strong></p>
 * <ul>
 *   <li>Most fields map automatically (same names)</li>
 *   <li>Audit fields (createTs, updateTs, deleteTs, etc.) ignored in DTO</li>
 *   <li>Version field ignored in DTO (internal optimistic locking)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UniversityMapper {

    // =====================================================
    // Entity → DTO
    // =====================================================

    /**
     * Convert University entity to UniversityDto
     *
     * <p>Audit fields are excluded from DTO (internal use only)</p>
     *
     * @param university entity
     * @return DTO
     */
    UniversityDto toDto(University university);

    /**
     * Convert list of University entities to list of DTOs
     *
     * @param universities entity list
     * @return DTO list
     */
    List<UniversityDto> toDtoList(List<University> universities);

    // =====================================================
    // DTO → Entity
    // =====================================================

    /**
     * Convert UniversityDto to University entity
     *
     * <p>Audit fields are NOT mapped from DTO (set by JPA callbacks)</p>
     *
     * @param dto DTO
     * @return entity
     */
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    University toEntity(UniversityDto dto);

    /**
     * Convert list of UniversityDtos to list of entities
     *
     * @param dtos DTO list
     * @return entity list
     */
    List<University> toEntityList(List<UniversityDto> dtos);

    // =====================================================
    // Partial Update (DTO → Entity)
    // =====================================================

    /**
     * Update existing University entity with non-null values from DTO
     *
     * <p>Used for PATCH operations - only updates non-null DTO fields</p>
     *
     * <p>Audit fields are NOT updated from DTO (managed by JPA callbacks)</p>
     *
     * @param dto source DTO
     * @param university target entity (will be modified)
     */
    @Mapping(target = "code", ignore = true)  // PK cannot be updated
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UniversityDto dto, @MappingTarget University university);
}
