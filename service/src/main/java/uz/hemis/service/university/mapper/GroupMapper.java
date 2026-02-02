package uz.hemis.service.university.mapper;

import org.mapstruct.*;
import uz.hemis.common.dto.GroupDto;
import uz.hemis.domain.entity.Group;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GroupMapper {
    GroupDto toDto(Group entity);

    @Mapping(target = "id", ignore = true)
    Group toEntity(GroupDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(GroupDto dto, @MappingTarget Group entity);
}
