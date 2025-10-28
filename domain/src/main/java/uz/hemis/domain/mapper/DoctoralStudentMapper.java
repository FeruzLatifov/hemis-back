package uz.hemis.domain.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.DoctoralStudentDto;
import uz.hemis.domain.entity.DoctoralStudent;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DoctoralStudentMapper {

    DoctoralStudentDto toDto(DoctoralStudent entity);

    List<DoctoralStudentDto> toDtoList(List<DoctoralStudent> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    DoctoralStudent toEntity(DoctoralStudentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTs", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateTs", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleteTs", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(DoctoralStudentDto dto, @MappingTarget DoctoralStudent entity);
}
