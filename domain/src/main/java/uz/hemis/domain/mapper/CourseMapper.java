package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.CourseDto;
import uz.hemis.domain.entity.Course;

/**
 * MapStruct mapper for Course entity ↔ CourseDto conversion
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
 * @see Course
 * @see CourseDto
 * @since 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CourseMapper {

    /**
     * Convert Course entity to CourseDto
     *
     * @param entity Course entity
     * @return CourseDto (null if entity is null)
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "creditCount", source = "creditCount")
    @Mapping(target = "totalHours", source = "totalHours")
    @Mapping(target = "lectureHours", source = "lectureHours")
    @Mapping(target = "practiceHours", source = "practiceHours")
    @Mapping(target = "labHours", source = "labHours")
    @Mapping(target = "semester", source = "semester")
    @Mapping(target = "courseType", source = "courseType")
    @Mapping(target = "assessmentType", source = "assessmentType")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isElective", source = "isElective")
    CourseDto toDto(Course entity);

    /**
     * Convert CourseDto to Course entity (for CREATE)
     *
     * @param dto CourseDto
     * @return Course entity (null if dto is null)
     */
    @Mapping(target = "id", ignore = true)  // Auto-generated
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "creditCount", source = "creditCount")
    @Mapping(target = "totalHours", source = "totalHours")
    @Mapping(target = "lectureHours", source = "lectureHours")
    @Mapping(target = "practiceHours", source = "practiceHours")
    @Mapping(target = "labHours", source = "labHours")
    @Mapping(target = "semester", source = "semester")
    @Mapping(target = "courseType", source = "courseType")
    @Mapping(target = "assessmentType", source = "assessmentType")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isElective", source = "isElective")
    // Audit fields ignored (managed by BaseEntity @PrePersist/@PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Course toEntity(CourseDto dto);

    /**
     * Update existing Course entity from CourseDto (for UPDATE/PATCH)
     *
     * @param dto    CourseDto with updated values
     * @param entity Existing Course entity to update
     */
    @Mapping(target = "id", ignore = true)  // Never update PK
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "creditCount", source = "creditCount")
    @Mapping(target = "totalHours", source = "totalHours")
    @Mapping(target = "lectureHours", source = "lectureHours")
    @Mapping(target = "practiceHours", source = "practiceHours")
    @Mapping(target = "labHours", source = "labHours")
    @Mapping(target = "semester", source = "semester")
    @Mapping(target = "courseType", source = "courseType")
    @Mapping(target = "assessmentType", source = "assessmentType")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "isElective", source = "isElective")
    // Audit fields ignored (managed by BaseEntity @PreUpdate)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(CourseDto dto, @MappingTarget Course entity);
}
