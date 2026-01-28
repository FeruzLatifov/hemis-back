package uz.hemis.service.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.CurriculumDto;
import uz.hemis.domain.entity.Curriculum;

/**
 * MapStruct mapper for Curriculum entity ↔ CurriculumDto conversion
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
 * @see Curriculum
 * @see CurriculumDto
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CurriculumMapper {

    /**
     * Convert Curriculum entity to CurriculumDto
     *
     * @param entity Curriculum entity
     * @return CurriculumDto (null if entity is null)
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "specialty", source = "specialty")
    @Mapping(target = "academicYear", source = "academicYear")
    @Mapping(target = "totalCredits", source = "totalCredits")
    @Mapping(target = "studyDuration", source = "studyDuration")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationForm", source = "educationForm")
    @Mapping(target = "curriculumType", source = "curriculumType")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isApproved", source = "isApproved")
    CurriculumDto toDto(Curriculum entity);

    /**
     * Convert CurriculumDto to Curriculum entity (for CREATE)
     *
     * @param dto CurriculumDto
     * @return Curriculum entity (null if dto is null)
     */
    @Mapping(target = "id", ignore = true)  // Auto-generated
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "specialty", source = "specialty")
    @Mapping(target = "academicYear", source = "academicYear")
    @Mapping(target = "totalCredits", source = "totalCredits")
    @Mapping(target = "studyDuration", source = "studyDuration")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationForm", source = "educationForm")
    @Mapping(target = "curriculumType", source = "curriculumType")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isApproved", source = "isApproved")
    // Audit fields ignored (managed by BaseEntity @PrePersist/@PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Curriculum toEntity(CurriculumDto dto);

    /**
     * Update existing Curriculum entity from CurriculumDto (for UPDATE/PATCH)
     *
     * @param dto    CurriculumDto with updated values
     * @param entity Existing Curriculum entity to update
     */
    @Mapping(target = "id", ignore = true)  // Never update PK
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "specialty", source = "specialty")
    @Mapping(target = "academicYear", source = "academicYear")
    @Mapping(target = "totalCredits", source = "totalCredits")
    @Mapping(target = "studyDuration", source = "studyDuration")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationForm", source = "educationForm")
    @Mapping(target = "curriculumType", source = "curriculumType")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isApproved", source = "isApproved")
    // Audit fields ignored (managed by BaseEntity @PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(CurriculumDto dto, @MappingTarget Curriculum entity);
}
