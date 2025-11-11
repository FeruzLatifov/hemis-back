package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.SpecialtyDto;
import uz.hemis.domain.entity.Specialty;

/**
 * MapStruct mapper for Specialty entity ↔ SpecialtyDto conversion
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
 * <p><strong>Specialty-Specific Fields:</strong></p>
 * <ul>
 *   <li>university: University code (VARCHAR PK reference)</li>
 *   <li>faculty: Faculty ID (UUID reference)</li>
 *   <li>specialtyType: Classifier code</li>
 *   <li>educationType: '11' = Bachelor, '12' = Master, '13' = PhD</li>
 *   <li>educationForm: '11' = Full-time, '12' = Part-time, '13' = Evening, '14' = Distance</li>
 *   <li>studyPeriod: Study period classifier code</li>
 * </ul>
 *
 * @see Specialty
 * @see SpecialtyDto
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SpecialtyMapper {

    /**
     * Convert Specialty entity to SpecialtyDto
     *
     * <p>Excludes audit fields (createTs, updateTs, etc.)</p>
     *
     * @param entity Specialty entity
     * @return SpecialtyDto (null if entity is null)
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "specialtyType", source = "specialtyType")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationForm", source = "educationForm")
    @Mapping(target = "studyPeriod", source = "studyPeriod")
    @Mapping(target = "active", source = "active")
    SpecialtyDto toDto(Specialty entity);

    /**
     * Convert SpecialtyDto to Specialty entity (for CREATE)
     *
     * <p>Excludes id (auto-generated) and audit fields (managed by BaseEntity)</p>
     *
     * @param dto SpecialtyDto
     * @return Specialty entity (null if dto is null)
     */
    @Mapping(target = "id", ignore = true)  // Auto-generated
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "specialtyType", source = "specialtyType")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationForm", source = "educationForm")
    @Mapping(target = "studyPeriod", source = "studyPeriod")
    @Mapping(target = "active", source = "active")
    // Audit fields ignored (managed by BaseEntity @PrePersist/@PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Specialty toEntity(SpecialtyDto dto);

    /**
     * Update existing Specialty entity from SpecialtyDto (for UPDATE/PATCH)
     *
     * <p>Only non-null DTO fields are applied to the entity.</p>
     * <p>Primary key (id) is never updated.</p>
     * <p>Audit fields are managed by BaseEntity @PreUpdate.</p>
     *
     * @param dto    SpecialtyDto with updated values
     * @param entity Existing Specialty entity to update
     */
    @Mapping(target = "id", ignore = true)  // Never update PK
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "specialtyType", source = "specialtyType")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationForm", source = "educationForm")
    @Mapping(target = "studyPeriod", source = "studyPeriod")
    @Mapping(target = "active", source = "active")
    // Audit fields ignored (managed by BaseEntity @PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(SpecialtyDto dto, @MappingTarget Specialty entity);
}
