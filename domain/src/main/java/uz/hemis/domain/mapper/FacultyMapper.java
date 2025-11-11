package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.FacultyDto;
import uz.hemis.domain.entity.Faculty;

/**
 * MapStruct mapper for Faculty entity ↔ FacultyDto conversion
 *
 * <p><strong>Mapping Strategy:</strong></p>
 * <ul>
 *   <li>Entity → DTO: All fields mapped (audit fields excluded)</li>
 *   <li>DTO → Entity: Business fields only (audit handled by BaseEntity)</li>
 *   <li>Partial Update: Only non-null DTO fields applied to entity</li>
 * </ul>
 *
 * <p><strong>Audit Fields Handling:</strong></p>
 * <ul>
 *   <li>toDto: Excludes createTs, updateTs, deleteTs, createdBy, updatedBy, deletedBy</li>
 *   <li>toEntity: Audit fields managed by service layer (@PrePersist/@PreUpdate)</li>
 * </ul>
 *
 * @see Faculty
 * @see FacultyDto
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FacultyMapper {

    /**
     * Convert Faculty entity to FacultyDto
     *
     * <p>Excludes audit fields (createTs, updateTs, etc.)</p>
     *
     * @param entity Faculty entity
     * @return FacultyDto (null if entity is null)
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "facultyType", source = "facultyType")
    @Mapping(target = "active", source = "active")
    FacultyDto toDto(Faculty entity);

    /**
     * Convert FacultyDto to Faculty entity (for CREATE)
     *
     * <p>Excludes id (auto-generated) and audit fields (managed by BaseEntity)</p>
     *
     * @param dto FacultyDto
     * @return Faculty entity (null if dto is null)
     */
    @Mapping(target = "id", ignore = true)  // Auto-generated
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "facultyType", source = "facultyType")
    @Mapping(target = "active", source = "active")
    // Audit fields ignored (managed by BaseEntity @PrePersist/@PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Faculty toEntity(FacultyDto dto);

    /**
     * Update existing Faculty entity from FacultyDto (for UPDATE/PATCH)
     *
     * <p>Only non-null DTO fields are applied to the entity.</p>
     * <p>Primary key (id) is never updated.</p>
     * <p>Audit fields are managed by BaseEntity @PreUpdate.</p>
     *
     * @param dto    FacultyDto with updated values
     * @param entity Existing Faculty entity to update
     */
    @Mapping(target = "id", ignore = true)  // Never update PK
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "facultyType", source = "facultyType")
    @Mapping(target = "active", source = "active")
    // Audit fields ignored (managed by BaseEntity @PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(FacultyDto dto, @MappingTarget Faculty entity);
}
