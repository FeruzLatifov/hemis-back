package uz.hemis.service.university.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.university.SpecialtyDto;
import uz.hemis.domain.entity.Specialty;

/**
 * MapStruct mapper for Specialty entity ↔ SpecialtyDto conversion
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

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationYear", source = "educationYear")
    @Mapping(target = "active", source = "active")
    SpecialtyDto toDto(Specialty entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationYear", source = "educationYear")
    @Mapping(target = "active", source = "active")
    Specialty toEntity(SpecialtyDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "university", source = "university")
    @Mapping(target = "faculty", source = "faculty")
    @Mapping(target = "educationType", source = "educationType")
    @Mapping(target = "educationYear", source = "educationYear")
    @Mapping(target = "active", source = "active")
    void updateEntityFromDto(SpecialtyDto dto, @MappingTarget Specialty entity);
}
