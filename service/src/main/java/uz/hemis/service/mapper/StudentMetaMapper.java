package uz.hemis.service.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.StudentMetaDto;
import uz.hemis.domain.entity.StudentMeta;

import java.util.List;

/**
 * StudentMeta Mapper - MapStruct Entity ↔ DTO conversion
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
 *   <li>Audit fields (createTs, deleteTs, etc.) ignored in DTO</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StudentMetaMapper {

    // =====================================================
    // Entity → DTO
    // =====================================================

    /**
     * Convert StudentMeta entity to StudentMetaDto
     *
     * <p>Audit fields are excluded from DTO (internal use only)</p>
     * <p>All fields from old-hemis are mapped for 100% backward compatibility</p>
     *
     * @param studentMeta entity
     * @return DTO
     */
    StudentMetaDto toDto(StudentMeta studentMeta);

    /**
     * Convert list of StudentMeta entities to DTOs
     *
     * @param studentMetas list of entities
     * @return list of DTOs
     */
    List<StudentMetaDto> toDtoList(List<StudentMeta> studentMetas);

    // =====================================================
    // DTO → Entity
    // =====================================================

    /**
     * Convert StudentMetaDto to StudentMeta entity
     *
     * <p><strong>CRITICAL - Audit Fields:</strong></p>
     * <ul>
     *   <li>createTs, createdBy - set by @PrePersist callback</li>
     *   <li>updateTs, updatedBy - set by @PreUpdate callback</li>
     *   <li>deleteTs, deletedBy - ignored (not in DTO)</li>
     *   <li>version - managed by JPA @Version</li>
     * </ul>
     *
     * @param dto DTO
     * @return entity
     */
    @Mapping(target = "createTs", ignore = true)       // Set by @PrePersist
    @Mapping(target = "createdBy", ignore = true)      // Set by @PrePersist
    @Mapping(target = "updateTs", ignore = true)       // Set by @PreUpdate
    @Mapping(target = "updatedBy", ignore = true)      // Set by @PreUpdate
    @Mapping(target = "deleteTs", ignore = true)       // Internal only
    @Mapping(target = "deletedBy", ignore = true)      // Internal only
    @Mapping(target = "version", ignore = true)        // Managed by JPA
    StudentMeta toEntity(StudentMetaDto dto);

    /**
     * Convert list of StudentMetaDtos to entities
     *
     * @param dtos list of DTOs
     * @return list of entities
     */
    List<StudentMeta> toEntityList(List<StudentMetaDto> dtos);

    // =====================================================
    // Update Entity from DTO
    // =====================================================

    /**
     * Update existing StudentMeta entity from DTO
     *
     * <p><strong>Use Case:</strong> Update (PUT/PATCH) operations</p>
     * <p>Only non-null DTO fields update the entity</p>
     *
     * @param dto source DTO
     * @param studentMeta target entity (modified in-place)
     */
    @Mapping(target = "id", ignore = true)             // PK never changes
    @Mapping(target = "createTs", ignore = true)       // Creation time frozen
    @Mapping(target = "createdBy", ignore = true)      // Creator frozen
    @Mapping(target = "updateTs", ignore = true)       // Set by @PreUpdate
    @Mapping(target = "updatedBy", ignore = true)      // Set by @PreUpdate
    @Mapping(target = "deleteTs", ignore = true)       // Internal only
    @Mapping(target = "deletedBy", ignore = true)      // Internal only
    @Mapping(target = "version", ignore = true)        // Managed by JPA
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(StudentMetaDto dto, @MappingTarget StudentMeta studentMeta);

    // =====================================================
    // Partial Update (for PATCH operations)
    // =====================================================

    /**
     * Partial update - only specified fields
     *
     * <p>Used for HTTP PATCH operations where only subset of fields are updated</p>
     *
     * @param dto source DTO (with partial data)
     * @param studentMeta target entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    void partialUpdate(StudentMetaDto dto, @MappingTarget StudentMeta studentMeta);

    // =====================================================
    // MapStruct will generate implementation class:
    // uz.hemis.service.mapper.StudentMetaMapperImpl
    //
    // Implementation is auto-generated at compile-time
    // Registered as Spring @Component
    // Injectable via @Autowired or constructor injection
    // =====================================================
}
