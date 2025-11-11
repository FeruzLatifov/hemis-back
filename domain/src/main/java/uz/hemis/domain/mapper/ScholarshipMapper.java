package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.ScholarshipDto;
import uz.hemis.domain.entity.Scholarship;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScholarshipMapper {

    ScholarshipDto toDto(Scholarship entity);

    List<ScholarshipDto> toDtoList(List<Scholarship> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Scholarship toEntity(ScholarshipDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(ScholarshipDto dto, @MappingTarget Scholarship entity);
}
