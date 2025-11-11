package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.TeacherDto;
import uz.hemis.domain.entity.Teacher;

import java.util.List;

/**
 * Teacher Mapper - MapStruct Entity ↔ DTO conversion
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
 *   <li>Computed field (fullName) set via @AfterMapping</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TeacherMapper {

    // =====================================================
    // Entity → DTO
    // =====================================================

    /**
     * Convert Teacher entity to TeacherDto
     *
     * <p>Audit fields are excluded from DTO (internal use only)</p>
     * <p>fullName is computed via @AfterMapping</p>
     *
     * @param teacher entity
     * @return DTO
     */
    @Mapping(target = "fullName", ignore = true)  // Set via @AfterMapping
    TeacherDto toDto(Teacher teacher);

    /**
     * After mapping: Set fullName from entity's getFullName() method
     *
     * @param teacher source entity
     * @param dto target DTO
     */
    @AfterMapping
    default void setFullName(Teacher teacher, @MappingTarget TeacherDto dto) {
        if (teacher != null && dto != null) {
            dto.setFullName(teacher.getFullName());
        }
    }

    /**
     * Convert list of Teacher entities to list of DTOs
     *
     * @param teachers entity list
     * @return DTO list
     */
    List<TeacherDto> toDtoList(List<Teacher> teachers);

    // =====================================================
    // DTO → Entity
    // =====================================================

    /**
     * Convert TeacherDto to Teacher entity
     *
     * <p>Audit fields are NOT mapped from DTO (set by BaseEntity JPA callbacks)</p>
     * <p>fullName is ignored (not stored in entity, computed on-the-fly)</p>
     *
     * @param dto DTO
     * @return entity
     */
    @Mapping(target = "fullName", ignore = true)  // Computed field, not stored
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Teacher toEntity(TeacherDto dto);

    /**
     * Convert list of TeacherDtos to list of entities
     *
     * @param dtos DTO list
     * @return entity list
     */
    List<Teacher> toEntityList(List<TeacherDto> dtos);

    // =====================================================
    // Partial Update (DTO → Entity)
    // =====================================================

    /**
     * Update existing Teacher entity with non-null values from DTO
     *
     * <p>Used for PATCH operations - only updates non-null DTO fields</p>
     *
     * <p>Audit fields are NOT updated from DTO (managed by BaseEntity JPA callbacks)</p>
     *
     * @param dto source DTO
     * @param teacher target entity (will be modified)
     */
    @Mapping(target = "id", ignore = true)  // PK cannot be updated
    @Mapping(target = "fullName", ignore = true)  // Computed field
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(TeacherDto dto, @MappingTarget Teacher teacher);
}
